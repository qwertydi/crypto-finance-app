package com.dmsc.cryptofinanceservice.controller;

import com.dmsc.cryptofinanceservice.model.rest.CreateWalletRequest;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletResponse;
import com.dmsc.cryptofinanceservice.model.rest.WalletResponse;
import com.dmsc.cryptofinanceservice.service.WalletAggregatorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping(path = WalletController.BASE_URL, produces = "application/json")
public class WalletController {
    static final String BASE_URL = "/v1/wallet";

    private final WalletAggregatorService walletAggregatorService;

    public WalletController(WalletAggregatorService walletAggregatorService) {
        this.walletAggregatorService = walletAggregatorService;
    }

    @PutMapping(name = "/json", consumes = "application/json")
    public ResponseEntity<CreateWalletResponse> manageWalletJson(@RequestBody CreateWalletRequest request, @RequestParam(value = "frequency") Duration frequency) {
        return walletAggregatorService.manageWallet(request, frequency);
    }

    /**
     * API will receive string and will attempt to parse the format:
     * <br>
     * Symbol	Quantity	Price
     * <br>
     * BTC	0.12345	37870.5058
     * <br>
     * ETH	4.89532	2004.9774
     *
     * @param request   String
     * @param frequency Duration
     * @return ResponseEntity<CreateWalletResponse>
     */
    @PutMapping(consumes = "text/plain")
    public ResponseEntity<CreateWalletResponse> manageWallet(@RequestBody String request, @RequestParam(value = "frequency") Duration frequency) {
        return walletAggregatorService.manageWallet(request, frequency);
    }

    /**
     * Get wallet info
     * Returns details based on last available data or based on specified date
     *
     * @param walletId String walletId
     * @param date     Instant
     * @return ResponseEntity<WalletResponse>
     */
    @GetMapping
    public ResponseEntity<WalletResponse> getWalletInfo(@RequestParam(value = "walletId") String walletId,
                                                        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date) {
        return walletAggregatorService.fetchWalletInfo(walletId, Optional.ofNullable(date));
    }

    /**
     * Update configuration for wallet
     * If frequency field is defined will update the job frequency for the wallet
     *
     * @param walletId  String walletId
     * @param frequency Duration interval for the job
     * @return ResponseEntity<Void>
     */
    @PostMapping(value = "/{walletId}")
    public ResponseEntity<Void> updateWallet(@PathVariable(value = "walletId") String walletId,
                                             @RequestParam(value = "frequency", required = false) Duration frequency) {
        return walletAggregatorService.updateWallet(walletId, Optional.ofNullable(frequency));
    }
}
