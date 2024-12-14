package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Interface that will hold the methods responsible for fetching information regarding the cryptocurrencies.
 */
public interface CryptoProvider {
    Mono<List<CryptoItemDto>> getAssetsBySymbols(List<String> listSymbols);
}
