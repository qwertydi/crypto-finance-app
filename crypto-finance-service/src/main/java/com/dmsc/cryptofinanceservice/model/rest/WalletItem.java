package com.dmsc.cryptofinanceservice.model.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class WalletItem {
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
}
