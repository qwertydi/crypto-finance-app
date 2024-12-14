package com.dmsc.cryptofinanceservice.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Data
public class CryptoItemDto {
    private String id;
    private String symbol;
    private String name;
    private BigDecimal price;
    private Instant timestamp;
}
