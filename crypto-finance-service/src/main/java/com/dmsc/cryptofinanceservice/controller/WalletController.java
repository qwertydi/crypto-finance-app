package com.dmsc.cryptofinanceservice.controller;

import com.dmsc.cryptofinanceservice.model.rest.CreateWalletRequest;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletResponse;
import com.dmsc.cryptofinanceservice.model.rest.WalletResponse;
import com.dmsc.cryptofinanceservice.service.WalletAggregatorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping(WalletController.BASE_URL)
public class WalletController {
    static final String BASE_URL = "/v1/wallet";

    private final WalletAggregatorService walletAggregatorService;

    public WalletController(WalletAggregatorService walletAggregatorService) {
        this.walletAggregatorService = walletAggregatorService;
    }

    @PostMapping("/json")
    public ResponseEntity<CreateWalletResponse> walletInfoJson(@RequestBody CreateWalletRequest request, @RequestParam(value = "frequency", required = false) Duration frequency) {
        return ResponseEntity.ok(walletAggregatorService.createWallet(request, frequency));
    }

    @PostMapping
    public ResponseEntity<CreateWalletResponse> walletInfo(@RequestBody String request, @RequestParam(value = "frequency", required = false) Duration frequency) {
        return ResponseEntity.ok(walletAggregatorService.createWallet(request, frequency));
    }

    @GetMapping
    public ResponseEntity<WalletResponse> getWalletInfo(@RequestParam(value = "walletId") String walletId,
                                                        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date) {
        return ResponseEntity.ok(walletAggregatorService.fetchWalletInfo(walletId, Optional.ofNullable(date)));
    }
}
