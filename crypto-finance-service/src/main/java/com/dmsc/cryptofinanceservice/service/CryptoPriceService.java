package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.exception.AssetDataNotFound;
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

    private static CryptoItemDto getCryptoItemDto(CryptoPriceEntity cryptoPriceEntity) {
        return CryptoItemDto.builder()
            .price(cryptoPriceEntity.getPrice())
            .name(cryptoPriceEntity.getName())
            .id(cryptoPriceEntity.getExternalId())
            .symbol(cryptoPriceEntity.getSymbol())
            .price(cryptoPriceEntity.getPrice())
            .build();
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

    /**
     * Method overload from {@link CryptoPriceService#getAssetLatestPrice(String, Instant)} without date.
     *
     * @param externalId CryptoAsset ExternalId
     * @return CryptoItemDto
     */
    public CryptoItemDto getAssetLatestPrice(String externalId) {
        return getAssetLatestPrice(externalId, null);
    }

    /**
     * Get latest price from database.
     * Will allow to search using with or without date {@link Instant}
     * Throws runtime exception {@link AssetDataNotFound} when no result is found
     *
     * @param externalId CryptoAsset ExternalId
     * @param date       Instant
     * @return CryptoItemDto
     */
    public CryptoItemDto getAssetLatestPrice(String externalId, Instant date) {
        CryptoPriceEntity lastByExternalId;
        if (date == null) {
            lastByExternalId = cryptoPriceRepository.findTopByExternalIdOrderByTimeDesc(externalId)
                .orElseThrow(AssetDataNotFound::new);
        } else {
            lastByExternalId = cryptoPriceRepository.findTopByExternalIdAndTimeOrderByTimeDesc(externalId, date)
                .orElseThrow(AssetDataNotFound::new);
        }
        return getCryptoItemDto(lastByExternalId);
    }
}
