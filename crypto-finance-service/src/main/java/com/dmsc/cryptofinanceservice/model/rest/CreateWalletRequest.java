package com.dmsc.cryptofinanceservice.model.rest;

import lombok.Data;

import java.util.List;

@Data
public class CreateWalletRequest {
    private List<WalletItem> wallet;
}
