package com.dmsc.cryptofinanceservice.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class WalletResponse {
    private BigDecimal total;
    private BigDecimal bestPerformance;
    private String bestAsset;
    private String worstAsset;
    private BigDecimal worstPerformance;
}
