package com.dmsc.cryptofinanceservice.service;

import com.dmsc.coincapjavasdk.WebClientAssetsRestSdk;
import com.dmsc.coincapjavasdk.model.request.CryptoDataRequest;
import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class ExternalCryptoProviderService implements CryptoProvider {

    private final WebClientAssetsRestSdk assetsReactiveSdk;

    public ExternalCryptoProviderService(WebClientAssetsRestSdk assetsReactiveSdk) {
        this.assetsReactiveSdk = assetsReactiveSdk;
    }

    @Override
    public Mono<List<CryptoItemDto>> getAssetsBySymbols(List<String> listSymbols) {
        CryptoDataRequest request = new CryptoDataRequest();
        request.setSearch(listSymbols);

        return assetsReactiveSdk.getAssetsAsync(request)
            .map(response -> response.getData().stream()
                .filter(item -> listSymbols.contains(item.getSymbol()))
                .map(item -> CryptoItemDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .symbol(item.getSymbol())
                    .price(new BigDecimal(item.getPriceUsd()))
                    .timestamp(Instant.ofEpochMilli(response.getTimestamp()))
                    .build())
                .toList());
    }
}
