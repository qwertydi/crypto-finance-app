package com.dmsc.coincapjavasdk;

import com.dmsc.coincapjavasdk.model.response.CryptoByIdData;
import com.dmsc.coincapjavasdk.model.response.CryptoData;
import com.dmsc.coincapjavasdk.model.request.CryptoDataRequest;
import com.dmsc.coincapjavasdk.model.request.CryptoHistoryRequest;
import com.dmsc.coincapjavasdk.model.response.CryptoPriceHistoryData;
import reactor.core.publisher.Mono;

public interface AssetsReactiveSdk extends AssetsSdk {

    @Override
    default CryptoData getAssets(CryptoDataRequest params) {
        return getAssetsAsync(params).block();
    }

    Mono<CryptoData> getAssetsAsync(CryptoDataRequest params);

    @Override
    default CryptoByIdData getAssetsById(String id) {
        return getAssetsByIdAsync(id).block();
    }

    Mono<CryptoByIdData> getAssetsByIdAsync(String id);

    @Override
    default CryptoPriceHistoryData getHistoryByAsset(String id, CryptoHistoryRequest params) {
        return getHistoryByAssetAsync(id, params).block();
    }


    Mono<CryptoPriceHistoryData> getHistoryByAssetAsync(String id, CryptoHistoryRequest params);
}
