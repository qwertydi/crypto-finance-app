package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.exception.AssetDataNotFound;
import com.dmsc.cryptofinanceservice.model.dto.CryptoHistoryDto;
import com.dmsc.cryptofinanceservice.model.dto.CryptoItemDto;
import com.dmsc.cryptofinanceservice.model.dto.WalletAssetDto;
import com.dmsc.cryptofinanceservice.model.entity.CryptoPriceEntity;
import com.dmsc.cryptofinanceservice.properties.WalletRequestProperties;
import com.dmsc.cryptofinanceservice.repository.CryptoPriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CryptoPriceService {

    private final WalletAssetService walletAssetService;
    private final CryptoPriceRepository cryptoPriceRepository;
    private final CryptoProvider cryptoProvider;
    private final CryptoCachingService cryptoCachingService;

    // configurable value for max number of threads per wallet
    private final int numberOfThreadsPerWallet;

    // Handle multiple simultaneous access to the hashmap
    private final Map<UUID, Queue<WalletAssetDto>> walletQueue = new ConcurrentHashMap<>();

    public CryptoPriceService(WalletAssetService walletAssetService,
                              CryptoPriceRepository cryptoPriceRepository,
                              CryptoProvider cryptoProvider,
                              CryptoCachingService cryptoCachingService, WalletRequestProperties walletRequestProperties) {
        this.walletAssetService = walletAssetService;
        this.cryptoPriceRepository = cryptoPriceRepository;
        this.cryptoProvider = cryptoProvider;
        this.cryptoCachingService = cryptoCachingService;
        this.numberOfThreadsPerWallet = walletRequestProperties.getNumberOfThreads();
        populateCacheWithExistingCryptoData();
    }

    private static CryptoItemDto getCryptoItemDto(CryptoPriceEntity cryptoPriceEntity) {
        return CryptoItemDto.builder()
            .price(cryptoPriceEntity.getPrice())
            .name(cryptoPriceEntity.getName())
            .id(cryptoPriceEntity.getExternalId())
            .symbol(cryptoPriceEntity.getSymbol())
            .price(cryptoPriceEntity.getPrice())
            .build();
    }

    private CryptoPriceEntity updateCryptoPrice(String cryptoAssetId) {
        CryptoItemDto cryptoItemDto = cryptoProvider.getAssetsById(Collections.singletonList(cryptoAssetId)).block().getFirst();
        // fetching first item, it will just fetch one asset
        if (cryptoItemDto == null) {
            log.warn("No info for cryptoAssetId: {}", cryptoAssetId);
            return null;
        }
        log.debug("Updating crypto price: {} timestamp: {}", cryptoAssetId, cryptoItemDto.getTimestamp());

        CryptoPriceEntity cryptoPriceEntity = new CryptoPriceEntity();
        cryptoPriceEntity.setPrice(cryptoItemDto.getPrice());
        cryptoPriceEntity.setSymbol(cryptoItemDto.getSymbol());
        cryptoPriceEntity.setTime(cryptoItemDto.getTimestamp());
        cryptoPriceEntity.setExternalId(cryptoItemDto.getId());
        cryptoPriceEntity.setName(cryptoItemDto.getName());

        log.info("End for: {}", cryptoAssetId);
        return cryptoPriceRepository.save(cryptoPriceEntity);
    }

    /**
     * Method responsible to fetch all unique data from {@link CryptoPriceEntity} and populate cache
     */
    private void populateCacheWithExistingCryptoData() {
        List<CryptoPriceEntity> allUniqueExternalIds = this.cryptoPriceRepository.findDistinctEntities();
        if (!CollectionUtils.isEmpty(allUniqueExternalIds)) {
            allUniqueExternalIds.forEach(existingUniqueEntities ->
                this.cryptoCachingService.addToCache(
                    existingUniqueEntities.getExternalId(),
                    existingUniqueEntities.getName(),
                    existingUniqueEntities.getSymbol()
                ));
        }
    }

    private Optional<CryptoPriceEntity> updateCryptoPriceByDate(String cryptoAssetId, Instant instant) {
        // Set before and after 1 day to get maximum data (?)
        CryptoHistoryDto cryptoItemDto = cryptoProvider.getAssetByIdAtGivenDate(cryptoAssetId, instant).block();
        // fetching first item, it will just fetch one asset
        if (cryptoItemDto == null) {
            log.warn("No info for cryptoAssetId: {}", cryptoAssetId);
            return Optional.empty();
        }

        List<CryptoPriceEntity> entitiesToAdd = cryptoItemDto.getCryptoHistory().stream()
            .map(item -> {
                CryptoPriceEntity cryptoPriceEntity = new CryptoPriceEntity();
                cryptoPriceEntity.setExternalId(cryptoAssetId);
                cryptoPriceEntity.setPrice(item.getPrice());
                cryptoPriceEntity.setTime(item.getTime());
                cryptoCachingService.getAssetDetails(cryptoAssetId)
                    .ifPresent(cachedItem -> {
                        cryptoPriceEntity.setName(cachedItem.getName());
                        cryptoPriceEntity.setSymbol(cachedItem.getSymbol());
                    });
                return cryptoPriceEntity;
            })
            .toList();
        log.info("End for: {}", cryptoAssetId);
        // Save all on the database but should only return the date matching the request..
        List<CryptoPriceEntity> cryptoPriceEntities = cryptoPriceRepository.saveAll(entitiesToAdd);

        // Get only the request that matches the minute of the input date
        Instant instantAtMinute = instant.truncatedTo(ChronoUnit.MINUTES);
        return cryptoPriceEntities.stream()
            .filter(entityDate -> entityDate.getTime().truncatedTo(ChronoUnit.MINUTES).equals(instantAtMinute))
            .findFirst();
    }

    /**
     * Batch processing of queue items.
     * Will receive a {@link Queue<WalletAssetDto>} and the walletId.
     * Until the queue is empty  it will group the items in groups of {@link CryptoPriceService#numberOfThreadsPerWallet}, polling from the queue until the value is matched.
     * To perform the batch of parallelized, it will create a List of {@link Mono} using {@link Mono#fromRunnable(Runnable)}.
     * The list will be run using {@link Schedulers#boundedElastic()} that will assure that each request will run on a seperated thread.
     * The {@link Mono#block(Duration)} in the end will ensure the requests are executed synchronously on the code block, before executing the next requests
     *
     * @param walletId UUID
     * @param queue    Queue of WalletAssetDto
     * @return List<CryptoPriceEntity>
     */
    private List<CryptoItemDto> processInBatches(UUID walletId, Queue<WalletAssetDto> queue, Instant date) {
        log.info("Now its {}", Instant.now());
        List<CryptoPriceEntity> results = new ArrayList<>();

        // Create a reactive queue (simulating fixed concurrency)
        while (!queue.isEmpty()) {
            // Collect the next 3 items in a batch
            List<WalletAssetDto> batch = new ArrayList<>(numberOfThreadsPerWallet);
            for (int i = 0; i < numberOfThreadsPerWallet && !queue.isEmpty(); i++) {
                batch.add(queue.poll());
            }

            // Process the batch asynchronously with controlled concurrency
            List<Mono<Object>> monos = batch.stream()
                .map(asset -> Mono.fromRunnable(() -> createBatchRequestEntry(walletId, asset, results, date))
                    .subscribeOn(Schedulers.boundedElastic())) // Use boundedElastic to limit concurrency
                .toList();

            // Merge all tasks in the batch and wait for completion
            Mono.when(monos).block();
        }

        log.info("Results processed: {}", results.size());
        return results.stream()
            .map(CryptoPriceService::getCryptoItemDto)
            .toList();
    }

    private CryptoPriceEntity updateCryptoPriceBySymbol(UUID walletId, Long walletAssetId, String cryptoAssetSymbol) {
        CryptoItemDto cryptoItemDto = cryptoProvider.getAssetsBySymbols(Collections.singletonList(cryptoAssetSymbol)).block().getFirst();
        // fetching first item, it will just fetch one asset
        log.debug("Updating crypto price: {} timestamp: {}", cryptoAssetSymbol, cryptoItemDto.getTimestamp());

        CryptoPriceEntity cryptoPriceEntity = new CryptoPriceEntity();
        cryptoPriceEntity.setPrice(cryptoItemDto.getPrice());
        cryptoPriceEntity.setSymbol(cryptoItemDto.getSymbol());
        cryptoPriceEntity.setTime(cryptoItemDto.getTimestamp());
        cryptoPriceEntity.setExternalId(cryptoItemDto.getId());
        cryptoPriceEntity.setName(cryptoItemDto.getName());

        CryptoPriceEntity save = cryptoPriceRepository.save(cryptoPriceEntity);

        // Update asset external id to allow to use coincap find by id API
        if (walletId != null && walletAssetId != null) {
            walletAssetService.updateWalletAsset(walletId, walletAssetId, cryptoItemDto);
            cryptoCachingService.addToCache(cryptoItemDto.getId(), cryptoItemDto.getName(), cryptoAssetSymbol);
        }

        log.info("End for: {}", cryptoItemDto.getId());
        return save;
    }

    /**
     * Method will use the walletId to fetch the Queue with {@link WalletAssetDto} and process the items in parallel manner
     *
     * @param walletId UUID
     */
    public void fetchWalletPrices(UUID walletId) {
        fetchWalletPricesManuallyTriggered(walletId, walletAssetService.findWalletAssetsByWalletId(walletId));
    }

    public List<CryptoItemDto> fetchWalletPricesManuallyTriggeredByDate(UUID walletId, List<WalletAssetDto> listWalletAssets, Instant date) {
        Queue<WalletAssetDto> queue;
        if (walletQueue.containsKey(walletId)) {
            queue = walletQueue.get(walletId);
        } else {
            queue = new LinkedList<>();
        }
        queue.addAll(listWalletAssets);
        walletQueue.put(walletId, queue);
        return processInBatches(walletId, walletQueue.get(walletId), date);
    }

    /**
     * Manually add List of {@link WalletAssetDto} for a specific wallet queue.
     * The queue for the wallet will then be processed in batch.
     *
     * @param walletId         WalletId
     * @param listWalletAssets List of {@link WalletAssetDto} to process
     * @return List<CryptoItemDto>
     */
    public List<CryptoItemDto> fetchWalletPricesManuallyTriggered(UUID walletId, List<WalletAssetDto> listWalletAssets) {
        return fetchWalletPricesManuallyTriggeredByDate(walletId, listWalletAssets, null);
    }

    private void createBatchRequestEntry(UUID walletId, WalletAssetDto asset, List<CryptoPriceEntity> results, Instant date) {
        log.info("Submitted request {} at {}", asset.getSymbol(), Instant.now());
        try {
            String externalId = asset.getExternalId();
            if (StringUtils.hasText(externalId)) {
                if (date != null) {
                    updateCryptoPriceByDate(externalId, date).ifPresent(results::add);
                } else {
                    results.add(updateCryptoPrice(externalId));
                }
            } else {
                results.add(updateCryptoPriceBySymbol(walletId, asset.getId(), asset.getSymbol()));
            }
        } catch (Exception e) {
            log.error("Error processing request for {}: {}", asset.getSymbol(), e.getMessage());
        }
    }

    /**
     * Method overload from {@link CryptoPriceService#getAssetLatestPrice(String, Instant)} without date.
     *
     * @param externalId CryptoAsset ExternalId
     * @return CryptoItemDto
     */
    public CryptoItemDto getAssetLatestPrice(String externalId) {
        return getAssetLatestPrice(externalId, null);
    }

    /**
     * Get latest price from database.
     * Will allow to search using with or without date {@link Instant}
     * Throws runtime exception {@link AssetDataNotFound} when no result is found
     *
     * @param externalId CryptoAsset ExternalId
     * @param date       Instant
     * @return CryptoItemDto
     */
    public CryptoItemDto getAssetLatestPrice(String externalId, Instant date) {
        CryptoPriceEntity lastByExternalId;
        if (date == null) {
            lastByExternalId = cryptoPriceRepository.findTopByExternalIdOrderByTimeDesc(externalId)
                .orElseThrow(AssetDataNotFound::new);
        } else {
            lastByExternalId = cryptoPriceRepository.findTopByExternalIdAndTimeOrderByTimeDesc(externalId, date, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(AssetDataNotFound::new);
        }
        return getCryptoItemDto(lastByExternalId);
    }
}
