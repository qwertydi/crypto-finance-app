package com.dmsc.cryptofinanceservice.properties;

import lombok.Data;

@Data
public class WalletRequestProperties {
    public static final String PREFIX = "wallet.requests";

    private int numberOfThreads = 3;
}
