package com.dmsc.cryptofinanceservice.controller;

import com.dmsc.cryptofinanceservice.model.rest.CreateWalletRequest;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletResponse;
import com.dmsc.cryptofinanceservice.model.rest.WalletItem;
import com.dmsc.cryptofinanceservice.model.rest.WalletResponse;
import com.dmsc.cryptofinanceservice.service.WalletAggregatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletControllerTest {

    private WalletAggregatorService mockWalletAggregatorService;

    private WalletController classUnderTest;

    @BeforeEach
    void setUp() {
        mockWalletAggregatorService = mock(WalletAggregatorService.class);
        classUnderTest = new WalletController(mockWalletAggregatorService);
    }

    @Nested
    class ManageWalletJson {

        @Test
        void testManageWalletJson() {
            // Creating a list of wallet items for the request
            WalletItem item1 = new WalletItem();
            item1.setSymbol("BTC");
            item1.setQuantity(new BigDecimal("0.12345"));
            item1.setPrice(new BigDecimal("37870.5058"));

            WalletItem item2 = new WalletItem();
            item2.setSymbol("ETH");
            item2.setQuantity(new BigDecimal("4.89532"));
            item2.setPrice(new BigDecimal("2004.9774"));

            CreateWalletRequest request = new CreateWalletRequest();
            request.setWallet(List.of(item1, item2));

            Duration frequency = Duration.ofMinutes(10);

            // Creating response with UUID
            CreateWalletResponse response = new CreateWalletResponse();
            response.setWalletId(UUID.randomUUID());

            when(mockWalletAggregatorService.manageWallet(request, frequency)).thenReturn(ResponseEntity.ok(response));

            ResponseEntity<CreateWalletResponse> result = classUnderTest.manageWalletJson(request, frequency);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody().getWalletId());
            verify(mockWalletAggregatorService, times(1)).manageWallet(request, frequency);
        }

        @ParameterizedTest
        @CsvSource({
            "BTC, 0.12345, 37870.5058, ETH, 4.89532, 2004.9774",
            "XRP, 10, 0.6789, LTC, 2, 300.567"
        })
        void testManageWalletJsonWithParameters(String symbol1, double quantity1, double price1,
                                                String symbol2, double quantity2, double price2) {
            WalletItem item1 = new WalletItem();
            item1.setSymbol(symbol1);
            item1.setQuantity(BigDecimal.valueOf(quantity1));
            item1.setPrice(BigDecimal.valueOf(price1));

            WalletItem item2 = new WalletItem();
            item2.setSymbol(symbol2);
            item2.setQuantity(BigDecimal.valueOf(quantity2));
            item2.setPrice(BigDecimal.valueOf(price2));

            CreateWalletRequest request = new CreateWalletRequest();
            request.setWallet(List.of(item1, item2));

            Duration frequency = Duration.ofMinutes(10);

            // Creating response with UUID
            CreateWalletResponse response = new CreateWalletResponse();
            response.setWalletId(UUID.randomUUID());

            when(mockWalletAggregatorService.manageWallet(request, frequency)).thenReturn(ResponseEntity.ok(response));

            ResponseEntity<CreateWalletResponse> result = classUnderTest.manageWalletJson(request, frequency);

            assertNotNull(result.getBody().getWalletId());
        }
    }

    @Nested
    class ManageWalletPlainText {

        @Test
        void testManageWalletPlainText() {
            // String formatted as symbol|quantity|price
            String requestBody = "BTC,0.12345,37870.5058\nETH,4.89532,2004.9774";
            Duration frequency = Duration.ofMinutes(15);

            // Creating response with UUID
            CreateWalletResponse response = new CreateWalletResponse();
            response.setWalletId(UUID.randomUUID());

            when(mockWalletAggregatorService.manageWallet(requestBody, frequency)).thenReturn(ResponseEntity.ok(response));

            ResponseEntity<CreateWalletResponse> result = classUnderTest.manageWallet(requestBody, frequency);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody().getWalletId());
            verify(mockWalletAggregatorService, times(1)).manageWallet(requestBody, frequency);
        }

        @ParameterizedTest
        @CsvSource({
            "BTC,0.12345,37870.5058,ETH,4.89532,2004.9774",
            "XRP,10,0.6789,LTC,2,300.567"
        })
        void testManageWalletPlainTextWithParameters(String symbol1, double quantity1, double price1,
                                                     String symbol2, double quantity2, double price2) {
            // Format input string as per provided table format
            String requestBody = String.format("%s,%s,%s\n%s,%s,%s",
                symbol1, quantity1, price1,
                symbol2, quantity2, price2);
            Duration frequency = Duration.ofMinutes(10);

            // Creating response with UUID
            CreateWalletResponse response = new CreateWalletResponse();
            response.setWalletId(UUID.randomUUID());

            when(mockWalletAggregatorService.manageWallet(requestBody, frequency)).thenReturn(ResponseEntity.ok(response));

            ResponseEntity<CreateWalletResponse> result = classUnderTest.manageWallet(requestBody, frequency);

            assertNotNull(result.getBody().getWalletId());
        }
    }

    @Nested
    class GetWalletInfo {

        @Test
        void testGetWalletInfoWithDate() {
            String walletId = "wallet1";
            Instant date = Instant.now();
            WalletResponse walletResponse = new WalletResponse();
            walletResponse.setTotal(BigDecimal.valueOf(1000));
            walletResponse.setBestPerformance(BigDecimal.valueOf(50));
            walletResponse.setBestAsset("BTC");
            walletResponse.setWorstAsset("ETH");
            walletResponse.setWorstPerformance(BigDecimal.valueOf(-10));

            when(mockWalletAggregatorService.fetchWalletInfo(walletId, Optional.of(date)))
                .thenReturn(ResponseEntity.ok(walletResponse));

            ResponseEntity<WalletResponse> result = classUnderTest.getWalletInfo(walletId, date);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(BigDecimal.valueOf(1000), result.getBody().getTotal());  // Assuming the total balance is used for validation here
            assertEquals("BTC", result.getBody().getBestAsset());
            verify(mockWalletAggregatorService, times(1)).fetchWalletInfo(walletId, Optional.of(date));
        }

        @Test
        void testGetWalletInfoWithoutDate() {
            String walletId = "wallet1";
            WalletResponse walletResponse = new WalletResponse();
            walletResponse.setTotal(BigDecimal.valueOf(1000));
            walletResponse.setBestPerformance(BigDecimal.valueOf(50));
            walletResponse.setBestAsset("BTC");
            walletResponse.setWorstAsset("ETH");
            walletResponse.setWorstPerformance(BigDecimal.valueOf(-10));

            when(mockWalletAggregatorService.fetchWalletInfo(walletId, Optional.empty()))
                .thenReturn(ResponseEntity.ok(walletResponse));

            ResponseEntity<WalletResponse> result = classUnderTest.getWalletInfo(walletId, null);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("BTC", result.getBody().getBestAsset());
            verify(mockWalletAggregatorService, times(1)).fetchWalletInfo(walletId, Optional.empty());
        }
    }

    @Nested
    class UpdateWallet {
        @Test
        void testUpdateWalletWithoutFrequency() {
            String walletId = "wallet1";

            when(mockWalletAggregatorService.updateWallet(walletId, Optional.empty())).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

            ResponseEntity<Void> result = classUnderTest.updateWallet(walletId, null);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(mockWalletAggregatorService, times(1)).updateWallet(walletId, Optional.empty());
        }
    }
}
