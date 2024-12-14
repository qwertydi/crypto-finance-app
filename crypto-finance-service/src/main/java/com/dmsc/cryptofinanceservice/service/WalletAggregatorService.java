package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.model.dto.WalletDto;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletRequest;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletResponse;
import com.dmsc.cryptofinanceservice.model.rest.WalletItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WalletAggregatorService {

    private final WalletService walletService;

    public WalletAggregatorService(WalletService walletService) {
        this.walletService = walletService;
    }

    public CreateWalletResponse createWallet(CreateWalletRequest request, Duration frequency) {
        WalletDto wallet = walletService.createWallet(frequency);

        // todo user request to add coins to wallet

        return CreateWalletResponse.builder()
            .walletId(wallet.getId())
            .build();
    }

    public CreateWalletResponse createWallet(String dataAsString, Duration frequency) {
        List<WalletItem> entries = new ArrayList<>();
        String[] lines = dataAsString.split("\n");

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String symbol = parts[0].trim();
                BigDecimal quantity = new BigDecimal(parts[1].trim());
                BigDecimal price = new BigDecimal(parts[2].trim());
                entries.add(new WalletItem(symbol, quantity, price));
            }
        }

        CreateWalletRequest request = new CreateWalletRequest();
        request.setWallet(entries);
        return createWallet(request, frequency);
    }
}
