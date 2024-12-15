package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import com.dmsc.cryptofinanceservice.model.dto.WalletAssetDto;
import com.dmsc.cryptofinanceservice.model.entity.WalletAssetEntity;
import com.dmsc.cryptofinanceservice.model.entity.WalletEntity;
import com.dmsc.cryptofinanceservice.repository.WalletAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletAssetServiceTest {

    private WalletAssetRepository mockWalletAssetRepository;

    private WalletAssetService classUnderTest;

    @BeforeEach
    void setUp() {
        mockWalletAssetRepository = mock(WalletAssetRepository.class);
        classUnderTest = new WalletAssetService(mockWalletAssetRepository);
    }

    @Test
    void testFindWalletAssetsByWalletId_WithAssets() {
        // Arrange
        UUID walletId = UUID.randomUUID();

        WalletEntity savedWalletEntity = new WalletEntity();
        savedWalletEntity.setId(walletId);

        WalletAssetEntity existingAsset = new WalletAssetEntity();
        existingAsset.setName("Bitcoin");
        existingAsset.setWallet(savedWalletEntity);
        existingAsset.setPrice(new BigDecimal(5000));
        existingAsset.setSymbol("BTC");
        existingAsset.setExternalId("external-1");
        existingAsset.setId(1L);
        WalletAssetEntity existingAsset2 = new WalletAssetEntity();
        existingAsset2.setName("Ethereum");
        existingAsset2.setWallet(savedWalletEntity);
        existingAsset2.setPrice(new BigDecimal(100));
        existingAsset2.setSymbol("ETH");
        existingAsset2.setExternalId("external-2");
        existingAsset2.setId(1L);

        List<WalletAssetEntity> assetList = List.of(
            existingAsset,
            existingAsset2
        );

        when(mockWalletAssetRepository.findByWalletId(walletId)).thenReturn(assetList);

        // Act
        List<WalletAssetDto> result = classUnderTest.findWalletAssetsByWalletId(walletId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("BTC", result.get(0).getSymbol());
        assertEquals("ETH", result.get(1).getSymbol());
    }

    @Test
    void testFindWalletAssetsByWalletId_NoAssets() {
        // Arrange
        UUID walletId = UUID.randomUUID();

        when(mockWalletAssetRepository.findByWalletId(walletId)).thenReturn(Collections.emptyList());

        // Act
        List<WalletAssetDto> result = classUnderTest.findWalletAssetsByWalletId(walletId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveWalletAssetsForWallet() {
        // Arrange
        UUID walletId = UUID.randomUUID();

        WalletEntity savedWalletEntity = new WalletEntity();
        savedWalletEntity.setId(walletId);

        WalletAssetEntity existingAsset = new WalletAssetEntity();
        existingAsset.setName("Bitcoin");
        existingAsset.setWallet(savedWalletEntity);
        existingAsset.setPrice(new BigDecimal(5000));
        existingAsset.setSymbol("BTC");
        existingAsset.setExternalId("external-1");
        existingAsset.setId(1L);
        WalletAssetEntity existingAsset2 = new WalletAssetEntity();
        existingAsset2.setName("Ethereum");
        existingAsset2.setWallet(savedWalletEntity);
        existingAsset2.setPrice(new BigDecimal(100));
        existingAsset2.setSymbol("ETH");
        existingAsset2.setExternalId("external-2");
        existingAsset2.setId(1L);

        List<WalletAssetEntity> assetList = List.of(
            existingAsset,
            existingAsset2
        );

        // Act
        classUnderTest.saveWalletAssetsForWallet(assetList);

        // Assert
        verify(mockWalletAssetRepository).saveAll(assetList);
    }

    @Test
    void testUpdateWalletAsset_ExistingAsset() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        WalletEntity savedWalletEntity = new WalletEntity();
        savedWalletEntity.setId(walletId);

        Long walletAssetId = 1L;
        CryptoItemDto cryptoItemDto = CryptoItemDto.builder()
            .id("external-1")
            .symbol("BTC")
            .name("Bitcoin Updated")
            .price(new BigDecimal(10000))
            .build();

        WalletAssetEntity existingAsset = new WalletAssetEntity();
        existingAsset.setName("Bitcoin");
        existingAsset.setWallet(savedWalletEntity);
        existingAsset.setPrice(new BigDecimal(5000));
        existingAsset.setSymbol("BTC");
        existingAsset.setExternalId("external-1");
        existingAsset.setId(1L);

        when(mockWalletAssetRepository.findByWalletIdAndId(walletId, walletAssetId)).thenReturn(Optional.of(existingAsset));

        // Act
        classUnderTest.updateWalletAsset(walletId, walletAssetId, cryptoItemDto);

        // Assert
        verify(mockWalletAssetRepository).findByWalletIdAndId(walletId, walletAssetId);
        verify(mockWalletAssetRepository).save(existingAsset);
        assertEquals("Bitcoin Updated", existingAsset.getName());
        assertEquals("external-1", existingAsset.getExternalId());
    }

    @Test
    void testUpdateWalletAsset_NonExistingAsset() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Long walletAssetId = 1L;
        CryptoItemDto cryptoItemDto = CryptoItemDto.builder()
            .id("external-1")
            .symbol("BTC")
            .name("Bitcoin Updated")
            .price(new BigDecimal(10000))
            .build();

        when(mockWalletAssetRepository.findByWalletIdAndId(walletId, walletAssetId)).thenReturn(Optional.empty());

        // Act
        classUnderTest.updateWalletAsset(walletId, walletAssetId, cryptoItemDto);

        // Assert
        verify(mockWalletAssetRepository).findByWalletIdAndId(walletId, walletAssetId);
        verify(mockWalletAssetRepository, never()).save(any(WalletAssetEntity.class));
    }
}

