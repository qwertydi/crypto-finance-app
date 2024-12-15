package com.dmsc.cryptofinanceservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CryptoHistoryItemDto {
    private BigDecimal price;
    private Instant time;
}
