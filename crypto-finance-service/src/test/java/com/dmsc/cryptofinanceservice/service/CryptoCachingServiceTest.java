package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.model.cache.CacheCryptoDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CryptoCachingServiceTest {

    private CryptoCachingService cryptoCachingService;

    @BeforeEach
    void setUp() {
        cryptoCachingService = new CryptoCachingService();
    }

    @Nested
    class AddToCache {

        @Test
        void shouldAddToCacheWhenIdNameAndSymbolAreValid() {
            String id = "123";
            String name = "Bitcoin";
            String symbol = "BTC";

            cryptoCachingService.addToCache(id, name, symbol);

            Optional<CacheCryptoDetails> result = cryptoCachingService.getAssetDetails(id);
            assertTrue(result.isPresent());
            assertEquals(name, result.get().getName());
            assertEquals(symbol, result.get().getSymbol());
        }

        @Test
        void shouldNotAddToCacheWhenIdIsInvalid() {
            String id = "";
            String name = "Bitcoin";
            String symbol = "BTC";

            cryptoCachingService.addToCache(id, name, symbol);

            Optional<CacheCryptoDetails> result = cryptoCachingService.getAssetDetails(id);
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldNotAddToCacheWhenNameOrSymbolIsInvalid() {
            String id = "123";
            String name = "";
            String symbol = "BTC";

            cryptoCachingService.addToCache(id, name, symbol);

            Optional<CacheCryptoDetails> result = cryptoCachingService.getAssetDetails(id);
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldNotOverwriteExistingCacheEntry() {
            String id = "123";
            String name1 = "Bitcoin";
            String symbol1 = "BTC";
            String name2 = "Ethereum";
            String symbol2 = "ETH";

            cryptoCachingService.addToCache(id, name1, symbol1);
            cryptoCachingService.addToCache(id, name2, symbol2);

            Optional<CacheCryptoDetails> result = cryptoCachingService.getAssetDetails(id);
            assertTrue(result.isPresent());
            assertEquals(name1, result.get().getName());
            assertEquals(symbol1, result.get().getSymbol());
        }
    }

    @Nested
    class GetAssetDetails {

        @Test
        void shouldReturnEmptyOptionalWhenIdDoesNotExist() {
            String id = "non-existent";

            Optional<CacheCryptoDetails> result = cryptoCachingService.getAssetDetails(id);
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnCachedDetailsWhenIdExists() {
            String id = "123";
            String name = "Bitcoin";
            String symbol = "BTC";

            cryptoCachingService.addToCache(id, name, symbol);

            Optional<CacheCryptoDetails> result = cryptoCachingService.getAssetDetails(id);
            assertTrue(result.isPresent());
            assertEquals(name, result.get().getName());
            assertEquals(symbol, result.get().getSymbol());
        }
    }
}

