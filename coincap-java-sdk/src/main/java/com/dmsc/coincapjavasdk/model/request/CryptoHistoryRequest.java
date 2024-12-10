package com.dmsc.coincapjavasdk.model.request;

import com.dmsc.coincapjavasdk.model.IntervalValue;
import lombok.Data;

import java.time.Instant;

@Data
public class CryptoHistoryRequest {
    private IntervalValue duration;
    // todo start and end, if any is set, are required
    private Instant start;
    private Instant end;
}
