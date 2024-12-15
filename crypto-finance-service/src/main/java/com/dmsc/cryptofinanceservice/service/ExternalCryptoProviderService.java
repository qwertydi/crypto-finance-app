package com.dmsc.cryptofinanceservice.service;

import com.dmsc.coincapjavasdk.WebClientAssetsRestSdk;
import com.dmsc.coincapjavasdk.model.IntervalValue;
import com.dmsc.coincapjavasdk.model.request.CryptoDataRequest;
import com.dmsc.coincapjavasdk.model.request.CryptoHistoryRequest;
import com.dmsc.coincapjavasdk.model.response.DataDetails;
import com.dmsc.cryptofinanceservice.model.dto.CryptoHistoryDto;
import com.dmsc.cryptofinanceservice.model.dto.CryptoHistoryItemDto;
import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Predicate;

@Service
public class ExternalCryptoProviderService implements CryptoProvider {

    private final WebClientAssetsRestSdk assetsReactiveSdk;

    public ExternalCryptoProviderService(WebClientAssetsRestSdk assetsReactiveSdk) {
        this.assetsReactiveSdk = assetsReactiveSdk;
    }

    @Override
    public Mono<List<CryptoItemDto>> getAssetsBySymbols(List<String> listSymbols) {
        return getAssets(listSymbols, null);
    }

    private Mono<List<CryptoItemDto>> getAssets(List<String> listSymbols, List<String> listIds) {
        CryptoDataRequest request = new CryptoDataRequest();
        if (!CollectionUtils.isEmpty(listSymbols)) {
            request.setSearch(listSymbols);
        }
        if (!CollectionUtils.isEmpty(listIds)) {
            request.setIds(listIds);
        }

        Predicate<DataDetails> dataDetailsPredicate = !CollectionUtils.isEmpty(listSymbols) ?
            item -> listSymbols.contains(item.getSymbol()) : item -> true;

        return assetsReactiveSdk.getAssetsAsync(request)
            .map(response -> response.getData().stream()
                .filter(dataDetailsPredicate)
                .map(item -> CryptoItemDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .symbol(item.getSymbol())
                    .price(new BigDecimal(item.getPriceUsd()))
                    .timestamp(Instant.ofEpochMilli(response.getTimestamp()))
                    .build())
                .toList());
    }

    @Override
    public Mono<List<CryptoItemDto>> getAssetsById(List<String> id) {
        return getAssets(null, id);
    }

    @Override
    public Mono<CryptoHistoryDto> getAssetByIdAtGivenDate(String id, Instant date) {
        CryptoHistoryRequest request = new CryptoHistoryRequest();
        // Find Date in 1 Minute Intervals to match minute timestamp
        request.setDuration(IntervalValue.M1);
        // To use M1 we only are able to search with 1 day or less periods
        request.setStart(date.minus(5, ChronoUnit.MINUTES));
        request.setEnd(date.plus(5, ChronoUnit.MINUTES));
        return assetsReactiveSdk.getHistoryByAssetAsync(id, request)
            .map(item -> {
                CryptoHistoryDto cryptoHistoryDto = new CryptoHistoryDto();
                cryptoHistoryDto.setDate(Instant.ofEpochMilli(item.getTimestamp()));
                cryptoHistoryDto.setCryptoHistory(item.getData().stream()
                    .map(historyEntry -> {
                        CryptoHistoryItemDto cryptoHistoryItemDto = new CryptoHistoryItemDto();
                        cryptoHistoryItemDto.setTime(Instant.ofEpochMilli(historyEntry.getTime()));
                        cryptoHistoryItemDto.setPrice(new BigDecimal(historyEntry.getPriceUsd()));
                        return cryptoHistoryItemDto;
                    }).toList());
                return cryptoHistoryDto;
            });
    }
}
