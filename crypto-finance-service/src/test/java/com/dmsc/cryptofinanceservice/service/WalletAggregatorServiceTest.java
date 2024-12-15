package com.dmsc.cryptofinanceservice.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import com.dmsc.cryptofinanceservice.model.dto.WalletAssetDto;
import com.dmsc.cryptofinanceservice.model.dto.WalletDto;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletRequest;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletResponse;
import com.dmsc.cryptofinanceservice.model.rest.WalletItem;
import com.dmsc.cryptofinanceservice.model.rest.WalletResponse;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

class WalletAggregatorServiceTest {

    private WalletAggregatorService walletAggregatorService;
    private WalletService walletService;
    private WalletAssetService walletAssetService;
    private CryptoPriceService cryptoPriceService;

    @BeforeEach
    void setUp() {
        walletService = mock(WalletService.class);
        walletAssetService = mock(WalletAssetService.class);
        cryptoPriceService = mock(CryptoPriceService.class);

        walletAggregatorService = new WalletAggregatorService(walletService, walletAssetService, cryptoPriceService);
    }

    @Nested
    class ManageWalletTests {

        @Test
        void shouldCreateWalletSuccessfullyWithRequest() {
            // Prepare the input data
            CreateWalletRequest request = new CreateWalletRequest();
            request.setWallet(List.of(new WalletItem("BTC", new BigDecimal("0.12345"), new BigDecimal("37870.5058"))));
            Duration frequency = Duration.ofHours(1);

            // Mock behavior
            WalletDto walletDto = new WalletDto();
            walletDto.setId(UUID.randomUUID());
            when(walletService.createWallet(eq(frequency), eq(request))).thenReturn(walletDto);

            // Execute method
            ResponseEntity<CreateWalletResponse> response = walletAggregatorService.manageWallet(request, frequency);

            // Capture arguments passed to the walletService
            ArgumentCaptor<CreateWalletRequest> createWalletRequestCaptor = ArgumentCaptor.forClass(CreateWalletRequest.class);
            ArgumentCaptor<Duration> frequencyCaptor = ArgumentCaptor.forClass(Duration.class);
            verify(walletService).createWallet(frequencyCaptor.capture(), createWalletRequestCaptor.capture());

            // Validate captured arguments
            assertEquals(frequency, frequencyCaptor.getValue());
            assertEquals(request, createWalletRequestCaptor.getValue());

            // Validate the response
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(walletDto.getId(), response.getBody().getWalletId());
        }

        @Test
        void shouldCreateWalletSuccessfullyWithDataAsString() {
            // Prepare the input data as string
            String dataAsString = "BTC,0.12345,37870.5058\nETH,4.89532,2004.9774";
            Duration frequency = Duration.ofHours(1);

            // Mock behavior for wallet creation
            WalletDto walletDto = new WalletDto();
            walletDto.setId(UUID.randomUUID());
            when(walletService.createWallet(eq(frequency), any(CreateWalletRequest.class))).thenReturn(walletDto);

            // Execute method
            ResponseEntity<CreateWalletResponse> response = walletAggregatorService.manageWallet(dataAsString, frequency);

            // Capture arguments passed to the walletService
            ArgumentCaptor<CreateWalletRequest> createWalletRequestCaptor = ArgumentCaptor.forClass(CreateWalletRequest.class);
            ArgumentCaptor<Duration> frequencyCaptor = ArgumentCaptor.forClass(Duration.class);
            verify(walletService).createWallet(frequencyCaptor.capture(), createWalletRequestCaptor.capture());

            // Validate captured arguments
            CreateWalletRequest capturedRequest = createWalletRequestCaptor.getValue();
            assertNotNull(capturedRequest);
            assertEquals(2, capturedRequest.getWallet().size()); // Two wallet items in the string
            assertEquals("BTC", capturedRequest.getWallet().get(0).getSymbol());
            assertEquals("ETH", capturedRequest.getWallet().get(1).getSymbol());

            // Validate the response
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(walletDto.getId(), response.getBody().getWalletId());
        }
    }

    @Nested
    class FetchWalletInfoTests {

        @Test
        void shouldFetchWalletInfoSuccessfully() {
            // Prepare mock data
            UUID walletId = UUID.randomUUID();
            Optional<Instant> date = Optional.empty();
            WalletDto walletDto = new WalletDto();
            walletDto.setId(walletId);

            List<WalletAssetDto> walletAssets = List.of(new WalletAssetDto(1L, "bitcoin", "BTC", "Bitcoin", BigDecimal.ONE, BigDecimal.valueOf(10000)));
            when(walletService.findWalletById(walletId)).thenReturn(walletDto);
            when(walletAssetService.findWalletAssetsByWalletId(walletDto.getId())).thenReturn(walletAssets);

            // Mock behavior for asset price fetch
            CryptoItemDto cryptoItemDto =  CryptoItemDto.builder()
                .id("bitcoin")
                .symbol("BTC")
                .name("Bitcoin")
                .price(new BigDecimal("37870.5058"))
                .timestamp(Instant.now())
                .build();

            when(cryptoPriceService.getAssetLatestPrice(anyString())).thenReturn(cryptoItemDto);

            // Execute method
            ResponseEntity<WalletResponse> response = walletAggregatorService.fetchWalletInfo(walletId.toString(), date);

            // Capture walletId argument
            ArgumentCaptor<UUID> walletIdCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(walletService).findWalletById(walletIdCaptor.capture());

            // Validate captured walletId
            assertEquals(walletId, walletIdCaptor.getValue());

            // Validate response
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(cryptoItemDto.getPrice().setScale(2, RoundingMode.HALF_UP), response.getBody().getTotal());
        }
    }

    @Nested
    class UpdateWalletTests {

        @Test
        void shouldUpdateWalletSuccessfully() {
            // Prepare test data
            String walletId = UUID.randomUUID().toString();
            Optional<Duration> frequency = Optional.of(Duration.ofHours(1));

            // Mock behavior for update wallet
            when(walletService.updateWalletIdConfigurations(eq(walletId), eq(frequency))).thenReturn(true);

            // Execute method
            ResponseEntity<Void> response = walletAggregatorService.updateWallet(walletId, frequency);

            // Validate response
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        void shouldNotUpdateWalletWhenNotModified() {
            // Prepare test data
            String walletId = UUID.randomUUID().toString();
            Optional<Duration> frequency = Optional.of(Duration.ofHours(1));

            // Mock behavior for update wallet
            when(walletService.updateWalletIdConfigurations(eq(walletId), eq(frequency))).thenReturn(false);

            // Execute method
            ResponseEntity<Void> response = walletAggregatorService.updateWallet(walletId, frequency);

            // Validate response
            assertEquals(HttpStatus.NOT_MODIFIED, response.getStatusCode());
        }
    }
}
