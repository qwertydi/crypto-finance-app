package com.dmsc.cryptofinanceservice.properties;

import lombok.Data;

import java.time.Instant;

@Data
public class JobServiceProperties {
    public static final String PREFIX = "jobs";

    private Instant jobDelayStartTime = Instant.ofEpochSecond(1);
}
