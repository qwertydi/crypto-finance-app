package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.exception.AssetDataNotFound;
import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import com.dmsc.cryptofinanceservice.model.dto.WalletAssetDto;
import com.dmsc.cryptofinanceservice.model.dto.WalletDto;
import com.dmsc.cryptofinanceservice.model.dto.WalletPerformance;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletRequest;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletResponse;
import com.dmsc.cryptofinanceservice.model.rest.WalletItem;
import com.dmsc.cryptofinanceservice.model.rest.WalletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WalletAggregatorService {

    private static final int DECIMAL_SCALE = 2;
    private static final int DIVISION_SCALE = 10;
    private static final BigDecimal DEFAULT_BIG_DECIMAL = new BigDecimal(0);

    private final WalletService walletService;
    private final WalletAssetService walletAssetService;
    private final CryptoPriceService cryptoPriceService;

    public WalletAggregatorService(WalletService walletService, WalletAssetService walletAssetService, CryptoPriceService cryptoPriceService) {
        this.walletService = walletService;
        this.walletAssetService = walletAssetService;
        this.cryptoPriceService = cryptoPriceService;
    }

    public CreateWalletResponse createWallet(CreateWalletRequest request, Duration frequency) {
        WalletDto wallet = walletService.createWallet(frequency, request);

        return CreateWalletResponse.builder()
            .walletId(wallet.getId())
            .build();
    }

    public CreateWalletResponse createWallet(String dataAsString, Duration frequency) {
        List<WalletItem> entries = new ArrayList<>();
        String[] lines = dataAsString.split("\n");

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String symbol = parts[0].trim();
                BigDecimal quantity = new BigDecimal(parts[1].trim());
                BigDecimal price = new BigDecimal(parts[DECIMAL_SCALE].trim());
                entries.add(new WalletItem(symbol, quantity, price));
            }
        }

        CreateWalletRequest request = new CreateWalletRequest();
        request.setWallet(entries);
        return createWallet(request, frequency);
    }

    public WalletResponse fetchWalletInfo(String walletId, Optional<Instant> date) {
        WalletDto wallet = walletService.findWalletById(UUID.fromString(walletId));

        List<WalletAssetDto> walletAssetsByWalletId = walletAssetService.findWalletAssetsByWalletId(wallet.getId());

        Map<WalletAssetDto, CryptoItemDto> assetData = new HashMap<>();
        List<WalletAssetDto> shouldFetchDataByDate = new ArrayList<>();
        List<WalletAssetDto> shouldFetchDataByCurrentDate = new ArrayList<>();
        for (WalletAssetDto itemDto : walletAssetsByWalletId) {
            if (date.isPresent()) {
                // Get DB result by date, or perform request
                try {
                    assetData.put(itemDto, cryptoPriceService.getAssetLatestPrice(itemDto.getExternalId(), date.get()));
                } catch (AssetDataNotFound exc) {
                    shouldFetchDataByDate.add(itemDto);
                }
            } else {
                try {
                    // Get latest DB result
                    assetData.put(itemDto, cryptoPriceService.getAssetLatestPrice(itemDto.getExternalId()));
                } catch (AssetDataNotFound exc) {
                    shouldFetchDataByCurrentDate.add(itemDto);
                }
            }
        }

        if (!CollectionUtils.isEmpty(shouldFetchDataByDate)) {
            log.info("Perform manually request by date: {}", date);
            // todo: implement fetch crypto data by date
        }
        if (!CollectionUtils.isEmpty(shouldFetchDataByCurrentDate)) {
            log.info("Perform manually request to get latest crypto data: {}", date);
            Map<String, CryptoItemDto> cryptoItemDtos = cryptoPriceService.fetchWalletPricesManuallyTriggered(wallet.getId(), shouldFetchDataByCurrentDate)
                .stream()
                .collect(Collectors.toMap(CryptoItemDto::getId, Function.identity()));
            shouldFetchDataByCurrentDate.forEach(i -> assetData.put(i, cryptoItemDtos.get(i.getExternalId())));
        }

        WalletPerformance walletPerformance = new WalletPerformance();
        assetData.forEach((itemDto, assetLatestPrice) -> {
            // Update total value
            updateTotalValue(walletPerformance, itemDto, assetLatestPrice);

            // Calculate asset performance
            BigDecimal assetPerformance = calculateAssetPerformance(assetLatestPrice.getPrice(), itemDto.getPrice());

            // Update best and worst-performing values
            updateBestPerforming(walletPerformance, assetPerformance, assetLatestPrice);
            updateWorstPerforming(walletPerformance, assetPerformance, assetLatestPrice);
        });

        return WalletResponse.builder()
            .total(walletPerformance.getTotalValue())
            .bestPerformance(walletPerformance.getBestPerformingValue())
            .bestAsset(walletPerformance.getBestPerformingAsset().getSymbol())
            .worstPerformance(walletPerformance.getWorstPerformingValue())
            .worstAsset(walletPerformance.getWorstPerformingAsset().getSymbol())
            .build();
    }

    /**
     * Set the wallet total value
     *
     * @param walletPerformance WalletPerformance
     * @param itemDto           WalletAssetDto
     * @param assetLatestPrice  CryptoItemDto
     */
    private void updateTotalValue(WalletPerformance walletPerformance, WalletAssetDto itemDto, CryptoItemDto assetLatestPrice) {
        BigDecimal currentTotalValue = Optional.ofNullable(walletPerformance.getTotalValue()).orElse(DEFAULT_BIG_DECIMAL);
        BigDecimal updatedTotalValue = currentTotalValue.add(
            assetLatestPrice.getPrice().multiply(itemDto.getQuantity())
        ).setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

        walletPerformance.setTotalValue(updatedTotalValue);
    }

    /**
     * Calculate the performance of a asset based on the initial price
     *
     * @param latestPrice  BigDecimal
     * @param initialPrice BigDecimal
     * @return BigDecimal
     */
    private BigDecimal calculateAssetPerformance(BigDecimal latestPrice, BigDecimal initialPrice) {
        if (latestPrice == null || initialPrice == null) {
            return DEFAULT_BIG_DECIMAL;
        }
        return latestPrice.subtract(initialPrice)
            .divide(initialPrice, DIVISION_SCALE, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Set best performing data
     *
     * @param walletPerformance WalletPerformance
     * @param assetPerformance  BigDecimal
     * @param assetLatestPrice  CryptoItemDto
     */
    private void updateBestPerforming(WalletPerformance walletPerformance, BigDecimal assetPerformance, CryptoItemDto assetLatestPrice) {
        if (walletPerformance.getBestPerformingValue() == null || assetPerformance.compareTo(walletPerformance.getBestPerformingValue()) > 0) {
            walletPerformance.setBestPerformingValue(assetPerformance);
            walletPerformance.setBestPerformingAsset(assetLatestPrice);
        }
    }

    /**
     * Set worst performing data
     *
     * @param walletPerformance WalletPerformance
     * @param assetPerformance  BigDecimal
     * @param assetLatestPrice  CryptoItemDto
     */
    private void updateWorstPerforming(WalletPerformance walletPerformance, BigDecimal assetPerformance, CryptoItemDto assetLatestPrice) {
        if (walletPerformance.getWorstPerformingValue() == null || assetPerformance.compareTo(walletPerformance.getWorstPerformingValue()) < 0) {
            walletPerformance.setWorstPerformingValue(assetPerformance);
            walletPerformance.setWorstPerformingAsset(assetLatestPrice);
        }
    }
}
