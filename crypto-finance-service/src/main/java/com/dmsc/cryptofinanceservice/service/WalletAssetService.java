package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import com.dmsc.cryptofinanceservice.model.dto.WalletAssetDto;
import com.dmsc.cryptofinanceservice.model.entity.WalletAssetEntity;
import com.dmsc.cryptofinanceservice.repository.WalletAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WalletAssetService {

    private final WalletAssetRepository walletAssetRepository;

    public WalletAssetService(WalletAssetRepository walletAssetRepository) {
        this.walletAssetRepository = walletAssetRepository;
    }

    public List<WalletAssetDto> findWalletAssetsByWalletId(UUID uuid) {
        List<WalletAssetEntity> walletAssets = walletAssetRepository.findByWalletId(uuid);
        if (CollectionUtils.isEmpty(walletAssets)) {
            return new ArrayList<>();
        }

        return walletAssets.stream()
            .map(asset ->
                WalletAssetDto.builder()
                    .id(asset.getId())
                    .externalId(asset.getExternalId())
                    .symbol(asset.getSymbol())
                    .name(asset.getName())
                    .build())
            .toList();
    }

    public void saveWalletAssetsForWallet(List<WalletAssetEntity> assetList) {
        walletAssetRepository.saveAll(assetList);
    }

    public void updateWalletAsset(UUID walletId, Long walletAssetId, CryptoItemDto cryptoItemDto) {
        // improvement: add cache to mat symbol to external id to avoid operation
        walletAssetRepository.findByWalletIdAndId(walletId, walletAssetId)
            .ifPresent(toUpdate -> {
                toUpdate.setName(cryptoItemDto.getName());
                toUpdate.setExternalId(cryptoItemDto.getId());
                walletAssetRepository.save(toUpdate);
            });
    }
}
