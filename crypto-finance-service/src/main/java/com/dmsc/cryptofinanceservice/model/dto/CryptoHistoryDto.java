package com.dmsc.cryptofinanceservice.model.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CryptoHistoryDto {
    private List<CryptoHistoryItemDto> cryptoHistory;
    private Instant date;
}
