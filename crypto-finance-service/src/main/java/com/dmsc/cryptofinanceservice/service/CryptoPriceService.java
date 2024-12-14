package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import com.dmsc.cryptofinanceservice.model.entity.CryptoPriceEntity;
import com.dmsc.cryptofinanceservice.repository.CryptoPriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class CryptoPriceService {

    private final WalletAssetService walletAssetService;
    private final CryptoPriceRepository cryptoPriceRepository;
    private final CryptoProvider cryptoProvider;

    public CryptoPriceService(WalletAssetService walletAssetService,
                              CryptoPriceRepository cryptoPriceRepository,
                              CryptoProvider cryptoProvider) {
        this.walletAssetService = walletAssetService;
        this.cryptoPriceRepository = cryptoPriceRepository;
        this.cryptoProvider = cryptoProvider;
    }

    public void updateCryptoPrice(String cryptoAssetId) {
        cryptoProvider.getAssetsById(Collections.singletonList(cryptoAssetId))
            .subscribe(item -> {
                // fetching first item, it will just fetch one asset
                CryptoItemDto cryptoItemDto = item.getFirst();
                log.debug("Updating crypto price: {} timestamp: {}", cryptoAssetId, cryptoItemDto.getTimestamp());

                CryptoPriceEntity cryptoPriceEntity = new CryptoPriceEntity();
                cryptoPriceEntity.setPrice(cryptoItemDto.getPrice());
                cryptoPriceEntity.setSymbol(cryptoItemDto.getSymbol());
                cryptoPriceEntity.setTime(cryptoItemDto.getTimestamp());
                cryptoPriceEntity.setExternalId(cryptoItemDto.getId());
                cryptoPriceRepository.save(cryptoPriceEntity);
            });
    }

    public void updateCryptoPriceBySymbol(UUID walletId, Long walletAssetId, String cryptoAssetSymbol) {
        cryptoProvider.getAssetsBySymbols(Collections.singletonList(cryptoAssetSymbol))
            .subscribe(item -> {
                // fetching first item, it will just fetch one asset
                CryptoItemDto cryptoItemDto = item.getFirst();
                log.debug("Updating crypto price: {} timestamp: {}", cryptoAssetSymbol, cryptoItemDto.getTimestamp());

                CryptoPriceEntity cryptoPriceEntity = new CryptoPriceEntity();
                cryptoPriceEntity.setPrice(cryptoItemDto.getPrice());
                cryptoPriceEntity.setSymbol(cryptoItemDto.getSymbol());
                cryptoPriceEntity.setTime(cryptoItemDto.getTimestamp());
                cryptoPriceEntity.setExternalId(cryptoItemDto.getId());
                cryptoPriceRepository.save(cryptoPriceEntity);

                // Update asset external id to allow to use coincap find by id API
                if (walletId != null && walletAssetId != null) {
                    walletAssetService.updateWalletAsset(walletId, walletAssetId, cryptoItemDto);
                }

            });
    }

    public void fetchWalletPrices(UUID walletId) {
        walletAssetService.findWalletAssetsByWalletId(walletId)
            .forEach(asset -> {
                // improvement: add a cache of symbol to external id
                if (StringUtils.hasText(asset.getExternalId())) {
                    updateCryptoPrice(asset.getExternalId());
                } else {
                    updateCryptoPriceBySymbol(walletId, asset.getId(), asset.getSymbol());
                    log.debug("No externalId for {}, walletId: {}", asset.getSymbol(), walletId);
                }
            });

    }
}
