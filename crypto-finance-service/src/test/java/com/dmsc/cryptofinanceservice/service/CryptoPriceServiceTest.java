package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.exception.AssetDataNotFound;
import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import com.dmsc.cryptofinanceservice.model.dto.WalletAssetDto;
import com.dmsc.cryptofinanceservice.model.entity.CryptoPriceEntity;
import com.dmsc.cryptofinanceservice.properties.WalletRequestProperties;
import com.dmsc.cryptofinanceservice.repository.CryptoPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CryptoPriceServiceTest {

    private WalletAssetService mockWalletAssetService;
    private CryptoPriceRepository mockCryptoPriceRepository;
    private CryptoProvider mockCryptoProvider;
    private CryptoCachingService mockCryptoCachingService;

    private CryptoPriceService cryptoPriceService;

    private static WalletAssetDto getWalletAssetDto(Long id, String name, String symbol, BigDecimal bigDecimal) {
        WalletAssetDto dto = new WalletAssetDto();
        dto.setId(id);
        dto.setName(name);
        dto.setSymbol(symbol);
        dto.setPrice(new BigDecimal("100000"));
        return dto;
    }

    public static CryptoPriceEntity createCryptoPriceEntity(Long id, String externalId, String name, String symbol, BigDecimal price, Instant time) {
        CryptoPriceEntity entity = new CryptoPriceEntity();
        entity.setId(id);
        entity.setExternalId(externalId);
        entity.setName(name);
        entity.setSymbol(symbol);
        entity.setPrice(price);
        entity.setTime(time);
        return entity;
    }

    @BeforeEach
    void setUp() {
        mockWalletAssetService = mock(WalletAssetService.class);
        mockCryptoPriceRepository = mock(CryptoPriceRepository.class);
        mockCryptoProvider = mock(CryptoProvider.class);
        mockCryptoCachingService = mock(CryptoCachingService.class);
        WalletRequestProperties walletRequestProperties = new WalletRequestProperties();

        cryptoPriceService = new CryptoPriceService(mockWalletAssetService, mockCryptoPriceRepository, mockCryptoProvider, mockCryptoCachingService, walletRequestProperties);
    }

    @Nested
    class PopulateCacheWithExistingCryptoData {

        @Test
        void shouldPopulateCacheWhenDistinctEntitiesExist() {
            CryptoPriceEntity entity = createCryptoPriceEntity(1L, "bitcoin", "Bitcoin", "BTC", BigDecimal.TEN, Instant.now());
            CryptoPriceEntity entity2 = createCryptoPriceEntity(2L, "ethereum", "Ethereum", "ETH", BigDecimal.ONE, Instant.now());


            List<CryptoPriceEntity> entities = List.of(
                entity,
                entity2
            );

            when(mockCryptoPriceRepository.findDistinctEntities()).thenReturn(entities);

            cryptoPriceService = new CryptoPriceService(mockWalletAssetService, mockCryptoPriceRepository, mockCryptoProvider, mockCryptoCachingService, new WalletRequestProperties());

            verify(mockCryptoCachingService, times(1)).addToCache("bitcoin", "Bitcoin", "BTC");
            verify(mockCryptoCachingService, times(1)).addToCache("ethereum", "Ethereum", "ETH");
        }

        @Test
        void shouldNotPopulateCacheWhenNoEntitiesExist() {
            when(mockCryptoPriceRepository.findDistinctEntities()).thenReturn(Collections.emptyList());

            cryptoPriceService = new CryptoPriceService(mockWalletAssetService, mockCryptoPriceRepository, mockCryptoProvider, mockCryptoCachingService, new WalletRequestProperties());

            verify(mockCryptoCachingService, never()).addToCache(any(), any(), any());
        }
    }

    @Nested
    class FetchWalletPrices {

        @Test
        void shouldProcessWalletPricesInBatches() {
            UUID walletId = UUID.randomUUID();
            List<WalletAssetDto> assets = List.of(
                getWalletAssetDto(1L, "bitcoin", "BTC", new BigDecimal("100000")),
                getWalletAssetDto(2L, "ethereum", "ETH", BigDecimal.TEN),
                getWalletAssetDto(3L, "solana", "SOL", new BigDecimal("0.01"))
            );

            when(mockWalletAssetService.findWalletAssetsByWalletId(walletId)).thenReturn(assets);
            when(mockCryptoProvider.getAssetsBySymbols(anyList())).thenReturn(Mono.just(List.of(
                CryptoItemDto.builder().id("bitcoin").symbol("BTC").price(BigDecimal.TEN).build(),
                CryptoItemDto.builder().id("ethereum").symbol("ETH").price(BigDecimal.ONE).build()
            )));
            CryptoPriceEntity entity = new CryptoPriceEntity();
            entity.setSymbol("BTC");
            entity.setExternalId("bitcoin");
            entity.setPrice(BigDecimal.TEN);
            CryptoPriceEntity entity2 = new CryptoPriceEntity();
            entity2.setSymbol("ETH");
            entity2.setExternalId("ethereum");
            entity2.setPrice(BigDecimal.ONE);

            when(mockCryptoPriceRepository.save(entity)).thenReturn(entity);
            when(mockCryptoPriceRepository.save(entity2)).thenReturn(entity2);

            cryptoPriceService.fetchWalletPrices(walletId);

            verify(mockWalletAssetService, times(1)).findWalletAssetsByWalletId(walletId);
            verify(mockCryptoProvider, times(3)).getAssetsBySymbols(anyList());
        }
    }

    @Nested
    class GetAssetLatestPrice {

        @Test
        void shouldReturnLatestPriceWithoutDate() {
            CryptoPriceEntity entity = createCryptoPriceEntity(1L, "bitcoin", "Bitcoin", "BTC", BigDecimal.TEN, Instant.now());

            when(mockCryptoPriceRepository.findTopByExternalIdOrderByTimeDesc("1")).thenReturn(Optional.of(entity));

            CryptoItemDto result = cryptoPriceService.getAssetLatestPrice("1");

            assertNotNull(result);
            assertEquals("bitcoin", result.getId());
            assertEquals("Bitcoin", result.getName());
            assertEquals("BTC", result.getSymbol());
            assertEquals(BigDecimal.TEN, result.getPrice());
        }

        @Test
        void shouldThrowExceptionWhenNoDataFound() {
            when(mockCryptoPriceRepository.findTopByExternalIdOrderByTimeDesc("1")).thenReturn(Optional.empty());

            assertThrows(AssetDataNotFound.class, () -> cryptoPriceService.getAssetLatestPrice("1"));
        }
    }
}

