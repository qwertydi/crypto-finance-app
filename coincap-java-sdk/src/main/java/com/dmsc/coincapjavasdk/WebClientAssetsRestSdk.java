package com.dmsc.coincapjavasdk;

import com.dmsc.coincapjavasdk.model.response.CryptoByIdData;
import com.dmsc.coincapjavasdk.model.response.CryptoData;
import com.dmsc.coincapjavasdk.model.request.CryptoDataRequest;
import com.dmsc.coincapjavasdk.model.request.CryptoHistoryRequest;
import com.dmsc.coincapjavasdk.model.response.CryptoPriceHistoryData;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class WebClientAssetsRestSdk implements AssetsReactiveSdk {

    private final WebClient webClient;

    public WebClientAssetsRestSdk(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<CryptoData> getAssetsAsync(CryptoDataRequest params) {
        return this.webClient.get()
            .uri(uriBuilder -> {
                UriBuilder path = uriBuilder.path("/assets");
                if (!CollectionUtils.isEmpty(params.getIds())) {
                    path.queryParam("ids", params.getIds());
                }
                if (!CollectionUtils.isEmpty(params.getSearch())) {
                    path.queryParam("search", params.getSearch());
                }
                Optional.ofNullable(params.getOffset())
                    .ifPresent(offset -> path.queryParam("offset", offset));
                Optional.ofNullable(params.getLimit())
                    .ifPresent(limit -> path.queryParam("limit", limit));
                return path.build();
            })
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(CryptoData.class);
    }

    @Override
    public Mono<CryptoByIdData> getAssetsByIdAsync(String id) {
        assertNotNull(id);
        return this.webClient.get()
            .uri(uriBuilder -> uriBuilder.path("/assets/" + id).build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(CryptoByIdData.class);
    }

    @Override
    public Mono<CryptoPriceHistoryData> getHistoryByAssetAsync(String id, CryptoHistoryRequest params) {
        assertNotNull(id);
        return this.webClient.get()
            .uri(uriBuilder -> {
                UriBuilder path = uriBuilder.path("/assets/" + id + "/history");
                Optional.ofNullable(params.getDuration())
                    .ifPresent(d -> path.queryParam("interval", d.name()));
                Optional.ofNullable(params.getStart())
                    .ifPresent(start -> path.queryParam("start", start));
                Optional.ofNullable(params.getEnd())
                    .ifPresent(end -> path.queryParam("end", end));
                return path.build();
            })
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(CryptoPriceHistoryData.class);
    }
}
