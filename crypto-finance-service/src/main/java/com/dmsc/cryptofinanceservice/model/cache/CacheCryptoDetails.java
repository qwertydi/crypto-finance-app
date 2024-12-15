package com.dmsc.cryptofinanceservice.model.cache;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class CacheCryptoDetails implements Serializable {
    private String externalId;
    private String symbol;
    private String name;
}
