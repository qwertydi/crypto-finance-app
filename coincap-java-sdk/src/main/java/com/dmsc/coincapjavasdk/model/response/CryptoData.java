package com.dmsc.coincapjavasdk.model.response;

import lombok.Data;

import java.util.List;

@Data
public class CryptoData {
    private List<DataDetails> data;
    private long timestamp;
}
