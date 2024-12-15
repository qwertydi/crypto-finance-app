package com.dmsc.cryptofinanceservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WalletPerformance {
    private BigDecimal totalValue;
    private BigDecimal bestPerformingValue;
    private BigDecimal worstPerformingValue;
    private CryptoItemDto bestPerformingAsset;
    private CryptoItemDto worstPerformingAsset;
}
