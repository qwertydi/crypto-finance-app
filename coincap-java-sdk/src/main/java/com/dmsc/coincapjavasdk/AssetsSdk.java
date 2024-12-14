package com.dmsc.coincapjavasdk;

import com.dmsc.coincapjavasdk.model.response.CryptoByIdData;
import com.dmsc.coincapjavasdk.model.response.CryptoData;
import com.dmsc.coincapjavasdk.model.request.CryptoDataRequest;
import com.dmsc.coincapjavasdk.model.request.CryptoHistoryRequest;
import com.dmsc.coincapjavasdk.model.response.CryptoPriceHistoryData;

public interface AssetsSdk {

    CryptoData getAssets(CryptoDataRequest params);

    CryptoByIdData getAssetsById(String id);

    CryptoPriceHistoryData getHistoryByAsset(String id, CryptoHistoryRequest params);
}
