package com.dmsc.coincapjavasdk.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class PriceData {
    private String priceUsd;
    private long time;
}
