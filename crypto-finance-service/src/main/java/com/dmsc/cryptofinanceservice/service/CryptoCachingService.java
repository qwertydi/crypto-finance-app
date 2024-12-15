package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.model.cache.CacheCryptoDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// improvement: fetch details from asset, since history API only will return price and timestamp
// improvement: denormalize data to have this information on separate table
@Service
public class CryptoCachingService {
    private final Map<String, CacheCryptoDetails> cryptoAssetCache;

    public CryptoCachingService() {
        cryptoAssetCache = new HashMap<>();
        // improvement populate cache by id on startup
    }

    public void addToCache(String id, String name, String symbol) {
        // Validate is ID is valid
        if (!StringUtils.hasText(id)) {
            return;
        }

        // Check if cache already exists
        if (!cryptoAssetCache.containsKey(id)
            && StringUtils.hasText(name)
            && StringUtils.hasText(symbol)) {
            // if not, add item to cache
            cryptoAssetCache.put(id, CacheCryptoDetails.builder()
                .externalId(id)
                .name(name)
                .symbol(symbol)
                .build());
        }
    }

    public Optional<CacheCryptoDetails> getAssetDetails(String id) {
        return Optional.ofNullable(cryptoAssetCache.get(id));
    }
}
