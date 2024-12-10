package com.dmsc.coincapjavasdk.model.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CryptoDataRequest {
    private List<String> search = new ArrayList<>();
    private List<String> ids = new ArrayList<>();
    private Integer limit;
    private Integer offset;
}
