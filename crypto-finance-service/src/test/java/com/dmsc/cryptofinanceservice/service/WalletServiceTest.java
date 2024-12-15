package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.exception.WalletNotFoundException;
import com.dmsc.cryptofinanceservice.model.dto.WalletDto;
import com.dmsc.cryptofinanceservice.model.entity.WalletEntity;
import com.dmsc.cryptofinanceservice.model.entity.WalletJobEntity;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletRequest;
import com.dmsc.cryptofinanceservice.model.rest.WalletItem;
import com.dmsc.cryptofinanceservice.repository.WalletJobRepository;
import com.dmsc.cryptofinanceservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletServiceTest {

    private WalletRepository mockWalletRepository;
    private WalletJobRepository mockWalletJobRepository;
    private WalletAssetService mockWalletAssetService;
    private JobService mockJobService;

    private WalletService classUnderTest;

    @BeforeEach
    void setUp() {
        mockWalletRepository = mock(WalletRepository.class);
        mockWalletJobRepository = mock(WalletJobRepository.class);
        mockWalletAssetService = mock(WalletAssetService.class);
        mockJobService = mock(JobService.class);

        this.classUnderTest = new WalletService(
            mockWalletRepository,
            mockWalletJobRepository,
            mockWalletAssetService,
            mockJobService
        );
    }

    @Test
    void testCreateWallet() {
        // Arrange
        UUID uuid = UUID.fromString("97340547-b00c-4903-a90d-6124d32f9eef");
        Duration frequency = Duration.ofHours(1);
        CreateWalletRequest request = new CreateWalletRequest();
        WalletDto expectedWalletDto = new WalletDto();
        expectedWalletDto.setId(uuid);
        WalletEntity savedWalletEntity = new WalletEntity();
        savedWalletEntity.setId(uuid);
        WalletJobEntity savedWalletJobEntity = new WalletJobEntity();

        WalletItem e1 = new WalletItem();
        e1.setPrice(new BigDecimal(5000));
        e1.setQuantity(new BigDecimal("0.1"));
        e1.setSymbol("BTC");
        WalletItem e2 = new WalletItem();
        e2.setPrice(new BigDecimal(1000));
        e2.setQuantity(new BigDecimal(10));
        e2.setSymbol("ETH");
        request.setWallet(List.of(
            e1,
            e2
        ));

        when(mockWalletRepository.save(any(WalletEntity.class))).thenReturn(savedWalletEntity);
        when(mockWalletJobRepository.save(any(WalletJobEntity.class))).thenReturn(savedWalletJobEntity);

        // Act
        WalletDto actualWalletDto = classUnderTest.createWallet(frequency, request);

        // Assert
        verify(mockWalletRepository).save(any(WalletEntity.class));
        verify(mockWalletJobRepository).save(any(WalletJobEntity.class));
        verify(mockWalletAssetService).saveWalletAssetsForWallet(anyList());
        verify(mockJobService).addOrUpdateJob(eq(uuid), eq(frequency));

        assertEquals(expectedWalletDto, actualWalletDto);
    }

    @Test
    void testFindWalletById_WalletExists() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        WalletEntity walletEntity = new WalletEntity();
        WalletDto expectedWalletDto = new WalletDto();

        when(mockWalletRepository.findById(walletId)).thenReturn(Optional.of(walletEntity));

        // Act
        WalletDto actualWalletDto = classUnderTest.findWalletById(walletId);

        // Assert
        verify(mockWalletRepository).findById(walletId);
        assertEquals(expectedWalletDto, actualWalletDto);
    }

    @Test
    void testFindWalletById_WalletNotFound() {
        // Arrange
        UUID walletId = UUID.randomUUID();

        when(mockWalletRepository.findById(walletId)).thenReturn(Optional.empty());

        // Act & Assert
        WalletNotFoundException exception = assertThrows(WalletNotFoundException.class, () -> classUnderTest.findWalletById(walletId));
        assertEquals("404 NOT_FOUND \"Wallet with id " + walletId + " not found\"", exception.getMessage());
    }

    @Test
    void testUpdateWalletIdConfigurations_FrequencyEmpty() {
        // Act
        boolean result = classUnderTest.updateWalletIdConfigurations(UUID.randomUUID().toString(), Optional.empty());

        // Assert
        assertFalse(result);
    }

    @Test
    void testUpdateWalletIdConfigurations_FrequencyPresent() {
        // Arrange
        UUID uuid = UUID.fromString("97340547-b00c-4903-a90d-6124d32f9eef");
        Duration frequency = Duration.ofHours(2);
        WalletEntity wallet = new WalletEntity();
        wallet.setId(uuid);
        WalletDto walletDto = new WalletDto();
        walletDto.setId(uuid);
        WalletJobEntity walletJobEntity = new WalletJobEntity();

        when(mockWalletRepository.findById(uuid)).thenReturn(Optional.of(wallet));
        when(mockWalletJobRepository.findByWalletId(walletDto.getId())).thenReturn(Optional.of(walletJobEntity));

        // Act
        boolean result = classUnderTest.updateWalletIdConfigurations(uuid.toString(), Optional.of(frequency));

        // Assert
        verify(mockWalletJobRepository).findByWalletId(walletDto.getId());
        verify(mockWalletJobRepository).save(walletJobEntity);
        verify(mockJobService).addOrUpdateJob(walletDto.getId(), frequency);
        assertTrue(result);
    }
}

