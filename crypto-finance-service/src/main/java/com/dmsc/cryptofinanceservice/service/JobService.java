package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.properties.JobServiceProperties;
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
    private final Instant jobStartTimeDelay;

    public JobService(CryptoPriceService cryptoPriceService, JobServiceProperties jobServiceProperties) {
        this.cryptoPriceService = cryptoPriceService;
        this.taskScheduler = getThreadPoolTaskScheduler();
        this.jobStartTimeDelay = jobServiceProperties.getJobDelayStartTime();
    }

    private static ThreadPoolTaskScheduler getThreadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    /**
     * Add or Update existing job duration.
     * Will check if walletId exists on cache and will update duration/frequency or create new entry.
     *
     * @param walletId UUID walletId
     * @param duration Duration
     */
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

    /**
     * Method responsible to check if there is an existing-scheduled job or if it can create a new one.
     * In case of existing job will attempt to cancel the task, before starting the updated one.
     * Scheduler uses the {@link ScheduledFuture} with {@link TaskScheduler} at a fixed rate.
     * The service responsible to fetch the data from the API and store in the database is the {@link CryptoPriceService#fetchWalletPrices(UUID)}
     *
     * @param walletId   UUID
     * @param jobDetails JobDetails
     */
    private void startJob(UUID walletId, JobDetails jobDetails) {
        // Cancel the previous scheduled job if it exists
        if (jobDetails.getSchedulerFuture() != null && !jobDetails.getSchedulerFuture().isCancelled()) {
            boolean canceled = jobDetails.getSchedulerFuture().cancel(false); // allow current execution to cancel
            if (canceled) {
                log.info("Previous job for walletId {} canceled successfully.", walletId);
            } else {
                log.warn("Previous job for walletId {} could not be canceled.", walletId);
            }
        }

        // Schedule new job
        ScheduledFuture<?> schedulerFuture = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                log.debug("Fetching wallet prices for walletId: {}", walletId);
                cryptoPriceService.fetchWalletPrices(walletId);
            } catch (Exception e) {
                log.error("Error while fetching wallet prices for walletId: {}", walletId, e);
            }
        }, jobStartTimeDelay, jobDetails.getDuration());

        // Update jobDetails with the new future
        jobDetails.setSchedulerFuture(schedulerFuture);
    }

    @Data
    public static class JobDetails {
        private Duration duration;
        private ScheduledFuture<?> schedulerFuture;
    }
}
