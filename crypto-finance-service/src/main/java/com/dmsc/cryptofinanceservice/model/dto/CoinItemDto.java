package com.dmsc.cryptofinanceservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinItemDto {
    private String externalId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
}
