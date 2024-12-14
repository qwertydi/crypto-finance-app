package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.exception.WalletNotFoundException;
import com.dmsc.cryptofinanceservice.model.dto.WalletDto;
import com.dmsc.cryptofinanceservice.model.entity.WalletAssetEntity;
import com.dmsc.cryptofinanceservice.model.entity.WalletEntity;
import com.dmsc.cryptofinanceservice.model.entity.WalletJobEntity;
import com.dmsc.cryptofinanceservice.model.rest.CreateWalletRequest;
import com.dmsc.cryptofinanceservice.repository.WalletAssetRepository;
import com.dmsc.cryptofinanceservice.repository.WalletJobRepository;
import com.dmsc.cryptofinanceservice.repository.WalletRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletJobRepository walletJobRepository;
    private final WalletAssetRepository walletAssetRepository;
    private final ModelMapper modelMapper;

    public WalletService(WalletRepository walletRepository,
                         WalletJobRepository walletJobRepository,
                         WalletAssetRepository walletAssetRepository) {
        this.walletRepository = walletRepository;
        this.walletJobRepository = walletJobRepository;
        this.walletAssetRepository = walletAssetRepository;
        this.modelMapper = new ModelMapper();
    }

    public WalletDto createWallet(Duration frequency, CreateWalletRequest request) {
        WalletEntity wallet = walletRepository.save(new WalletEntity());
        WalletJobEntity walletJobEntity = new WalletJobEntity();
        walletJobEntity.setWallet(wallet);
        walletJobEntity.setFrequency(frequency);
        walletJobRepository.save(walletJobEntity);

        List<WalletAssetEntity> assetList = new ArrayList<>();
        // external id and name missing, update on API call (?)
        request.getWallet()
            .forEach(asset -> {
                WalletAssetEntity assetEntity = new WalletAssetEntity();
                assetEntity.setSymbol(asset.getSymbol());
                assetEntity.setPrice(asset.getPrice());
                assetEntity.setQuantity(asset.getQuantity());
                assetEntity.setWallet(wallet);
                assetList.add(assetEntity);
            });
        walletAssetRepository.saveAll(assetList);
        return modelMapper.map(wallet, WalletDto.class);
    }

    public WalletDto findWalletById(UUID uuid) {
        WalletEntity wallet = walletRepository.findById(uuid)
            .orElseThrow(() -> new WalletNotFoundException("Wallet with id " + uuid + " not found"));
        return modelMapper.map(wallet, WalletDto.class);
    }
}
