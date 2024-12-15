package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.properties.JobServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class JobServiceTest {

    private static final UUID WALLET_ID = UUID.fromString("97340547-b00c-4903-a90d-6124d32f9eef");
    private CryptoPriceService mockCryptoPriceService;
    private TaskScheduler mockTaskScheduler;
    private ScheduledFuture<?> mockScheduledFuture;
    private JobService classUnderTest;

    @BeforeEach
    void setUp() {
        // Mock job delay start time
        mockCryptoPriceService = mock(CryptoPriceService.class);
        mockScheduledFuture = mock(ScheduledFuture.class);
        mockTaskScheduler = mock(TaskScheduler.class);

        JobServiceProperties properties = new JobServiceProperties();
        properties.setJobDelayStartTime(Instant.ofEpochMilli(0));


        classUnderTest = new JobService(mockCryptoPriceService, properties);
        classUnderTest.setTaskScheduler(mockTaskScheduler);
    }

    @Test
    void testAddOrUpdateJob_CreatesNewJob() {
        // Arrange
        Duration duration = Duration.ofMinutes(5);

        // Act
        classUnderTest.addOrUpdateJob(WALLET_ID, duration);

        // Assert
        verify(mockTaskScheduler).scheduleAtFixedRate(any(Runnable.class), any(Instant.class), eq(duration));
    }

    @Test
    void testAddOrUpdateJob_UpdatesExistingJob() {
        // Arrange
        Duration oldDuration = Duration.ofMinutes(5);
        Duration newDuration = Duration.ofMinutes(10);

        // Set up existing job
        JobService.JobDetails existingJob = new JobService.JobDetails();
        existingJob.setDuration(oldDuration);
        existingJob.setSchedulerFuture(mockScheduledFuture);

        ScheduledFuture mockScheduledFuture = mock(ScheduledFuture.class);
        when(mockTaskScheduler.scheduleAtFixedRate(any(), any(), any())).thenReturn(mockScheduledFuture);

        // Act
        classUnderTest.addOrUpdateJob(WALLET_ID, oldDuration);
        classUnderTest.addOrUpdateJob(WALLET_ID, newDuration);

        // Assert
        ArgumentCaptor<Duration> durationArgumentCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(mockTaskScheduler, times(2)).scheduleAtFixedRate(any(Runnable.class), any(Instant.class), durationArgumentCaptor.capture());

        verify(mockScheduledFuture).cancel(false);
        assertTrue(durationArgumentCaptor.getAllValues().contains(oldDuration));
        assertTrue(durationArgumentCaptor.getAllValues().contains(newDuration));
        assertFalse(durationArgumentCaptor.getAllValues().contains(Duration.ZERO));
    }

    @Test
    void testStartJob_SchedulesNewJob() {
        // Arrange
        Duration duration = Duration.ofMinutes(15);

        // Act
        classUnderTest.addOrUpdateJob(WALLET_ID, duration);

        // Assert
        verify(mockTaskScheduler).scheduleAtFixedRate(any(Runnable.class), any(Instant.class), eq(duration));
    }

    @Test
    void testScheduledTask_ExecutesCryptoPriceFetch() {
        // Arrange
        Duration duration = Duration.ofMinutes(10);

        when(mockTaskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), eq(duration)))
            .thenAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run(); // Simulate the task execution
                return mockScheduledFuture;
            });

        // Act
        classUnderTest.addOrUpdateJob(WALLET_ID, duration);

        // Assert
        verify(mockCryptoPriceService).fetchWalletPrices(WALLET_ID);
    }
}
