package com.dmsc.cryptofinanceservice.service;

import com.dmsc.coincapjavasdk.WebClientAssetsRestSdk;
import com.dmsc.coincapjavasdk.model.request.CryptoDataRequest;
import com.dmsc.coincapjavasdk.model.request.CryptoHistoryRequest;
import com.dmsc.coincapjavasdk.model.response.CryptoData;
import com.dmsc.coincapjavasdk.model.response.CryptoPriceHistoryData;
import com.dmsc.coincapjavasdk.model.response.DataDetails;
import com.dmsc.coincapjavasdk.model.response.PriceData;
import com.dmsc.cryptofinanceservice.model.dto.CryptoHistoryDto;
import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExternalCryptoProviderServiceTest {

    private WebClientAssetsRestSdk mockAssetsReactiveSdk;

    private ExternalCryptoProviderService classUnderTest;

    @BeforeEach
    void setup() {
        mockAssetsReactiveSdk = mock(WebClientAssetsRestSdk.class);
        classUnderTest = new ExternalCryptoProviderService(mockAssetsReactiveSdk);
    }

    @Nested
    class GetAssetsBySymbols {
        @Test
        void shouldReturnAssetsWhenSymbolsAreProvided() {
            // Arrange
            List<String> symbols = List.of("BTC", "ETH");
            CryptoData mockResponse = new CryptoData();
            mockResponse.setData(List.of(
                createDataDetails("BTC", "Bitcoin", "50000.00"),
                createDataDetails("ETH", "Ethereum", "4000.00")
            ));
            mockResponse.setTimestamp(Instant.now().toEpochMilli());

            when(mockAssetsReactiveSdk.getAssetsAsync(any(CryptoDataRequest.class)))
                .thenReturn(Mono.just(mockResponse));

            // Act
            List<CryptoItemDto> result = classUnderTest.getAssetsBySymbols(symbols).block();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("BTC", result.get(0).getSymbol());
            assertEquals(new BigDecimal("50000.00"), result.get(0).getPrice());
        }

        @Test
        void shouldReturnEmptyListWhenNoMatchingSymbols() {
            // Arrange
            List<String> symbols = List.of("DOGE");
            CryptoData mockResponse = new CryptoData();
            mockResponse.setData(List.of(
                createDataDetails("BTC", "Bitcoin", "50000.00")
            ));
            mockResponse.setTimestamp(Instant.now().toEpochMilli());

            when(mockAssetsReactiveSdk.getAssetsAsync(any(CryptoDataRequest.class)))
                .thenReturn(Mono.just(mockResponse));

            // Act
            List<CryptoItemDto> result = classUnderTest.getAssetsBySymbols(symbols).block();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetAssetsById {
        @Test
        void shouldReturnAssetsWhenIdsAreProvided() {
            // Arrange
            List<String> ids = List.of("1", "2");
            CryptoData mockResponse = new CryptoData();
            mockResponse.setData(List.of(
                createDataDetails("BTC", "Bitcoin", "50000.00", "1"),
                createDataDetails("ETH", "Ethereum", "4000.00", "2")
            ));
            mockResponse.setTimestamp(Instant.now().toEpochMilli());

            when(mockAssetsReactiveSdk.getAssetsAsync(any(CryptoDataRequest.class)))
                .thenReturn(Mono.just(mockResponse));

            // Act
            List<CryptoItemDto> result = classUnderTest.getAssetsById(ids).block();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("1", result.get(0).getId());
        }
    }

    @Nested
    class GetAssetByIdAtGivenDate {
        @Test
        void shouldReturnCryptoHistoryWhenIdAndDateAreProvided() {
            // Arrange
            String id = "1";
            Instant date = Instant.now();
            CryptoPriceHistoryData mockResponse = new CryptoPriceHistoryData();
            mockResponse.setData(List.of(
                createHistoryEntry("45000.00", date.minusSeconds(300).toEpochMilli()),
                createHistoryEntry("46000.00", date.toEpochMilli())
            ));
            mockResponse.setTimestamp(Instant.now().toEpochMilli());

            when(mockAssetsReactiveSdk.getHistoryByAssetAsync(eq(id), any(CryptoHistoryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

            // Act
            CryptoHistoryDto result = classUnderTest.getAssetByIdAtGivenDate(id, date).block();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getCryptoHistory().size());
            assertEquals(new BigDecimal("46000.00"), result.getCryptoHistory().get(1).getPrice());
        }
    }

    // Helper methods
    private DataDetails createDataDetails(String symbol, String name, String priceUsd) {
        return createDataDetails(symbol, name, priceUsd, null);
    }

    private DataDetails createDataDetails(String symbol, String name, String priceUsd, String id) {
        DataDetails dataDetails = new DataDetails();
        dataDetails.setSymbol(symbol);
        dataDetails.setName(name);
        dataDetails.setPriceUsd(priceUsd);
        dataDetails.setId(id);
        return dataDetails;
    }

    private PriceData createHistoryEntry(String priceUsd, long timestamp) {
        PriceData historyEntry = new PriceData();
        historyEntry.setPriceUsd(priceUsd);
        historyEntry.setTime(timestamp);
        return historyEntry;
    }
}
