package com.dmsc.coincapjavasdk.model.response;

import lombok.Data;

import java.util.List;

@Data
public class CryptoPriceHistoryData {
    private List<PriceData> data;
    private long timestamp;
}
