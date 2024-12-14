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
public class WalletAssetDto {
    private Long id;
    private String externalId;
    private String symbol;
    private String name;
    private BigDecimal quantity;
    private BigDecimal price;
}
