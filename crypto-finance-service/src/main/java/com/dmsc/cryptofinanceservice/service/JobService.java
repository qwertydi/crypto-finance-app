package com.dmsc.cryptofinanceservice.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class JobService {
    private final CryptoPriceService cryptoPriceService;
    private final TaskScheduler taskScheduler;
    private final Map<UUID, JobDetails> walletJobs = new HashMap<>();

    public JobService(CryptoPriceService cryptoPriceService) {
        this.cryptoPriceService = cryptoPriceService;
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.initialize();
        this.taskScheduler = threadPoolTaskScheduler;
    }

    public void addOrUpdateJob(UUID walletId, Duration duration) {
        JobDetails jobDetails;
        if (walletJobs.containsKey(walletId)) {
            jobDetails = walletJobs.get(walletId);
            log.info("Updating wallet job id: {} frequency from: {} to: {}", walletId, jobDetails.getDuration(), duration);
            jobDetails.setDuration(duration);
        } else {
            jobDetails = new JobDetails();
            jobDetails.setDuration(duration);
            log.info("Creating wallet job id: {} with frequency: {}", walletId, duration);
        }

        startJob(walletId, jobDetails);

        walletJobs.put(walletId, jobDetails);
    }

    private void startJob(UUID walletId, JobDetails jobDetails) {
        // init the job
        if (jobDetails.getSchedulerFuture() == null) {
            ScheduledFuture<?> schedulerFuture = taskScheduler.scheduleAtFixedRate(() -> cryptoPriceService.fetchWalletPrices(walletId), Instant.ofEpochSecond(0), jobDetails.getDuration());
            jobDetails.setSchedulerFuture(schedulerFuture);
        } else {
            jobDetails.getSchedulerFuture().cancel(false); // allow current execution to cancel
            ScheduledFuture<?> schedulerFuture = taskScheduler.scheduleAtFixedRate(() -> cryptoPriceService.fetchWalletPrices(walletId), Instant.ofEpochSecond(0), jobDetails.getDuration());
            jobDetails.setSchedulerFuture(schedulerFuture);
        }
    }

    @Data
    public static class JobDetails {
        private Duration duration;
        private ScheduledFuture<?> schedulerFuture;
    }
}
