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
    public Mono<CryptoHistoryDto> getAssetByIdAtGivenDate(String id, Instant start, Instant end) {
        CryptoHistoryRequest request = new CryptoHistoryRequest();
        // 1 Hour should be enough to fetch data for a given crypto coin on a given day
        request.setDuration(IntervalValue.H1);
        request.setStart(start);
        request.setEnd(end);
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
