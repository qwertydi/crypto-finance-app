package com.dmsc.cryptofinanceservice.service;

import com.dmsc.cryptofinanceservice.exception.WalletNotFoundException;
import com.dmsc.cryptofinanceservice.model.dto.WalletDto;
import com.dmsc.cryptofinanceservice.model.entity.WalletEntity;
import com.dmsc.cryptofinanceservice.model.entity.WalletJobEntity;
import com.dmsc.cryptofinanceservice.repository.WalletJobRepository;
import com.dmsc.cryptofinanceservice.repository.WalletRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletJobRepository walletJobRepository;
    private final ModelMapper modelMapper;

    public WalletService(WalletRepository walletRepository, WalletJobRepository walletJobRepository) {
        this.walletRepository = walletRepository;
        this.walletJobRepository = walletJobRepository;
        this.modelMapper = new ModelMapper();
    }

    public WalletDto createWallet(Duration frequency) {
        WalletEntity wallet = walletRepository.save(new WalletEntity());
        WalletJobEntity walletJobEntity = new WalletJobEntity();
        walletJobEntity.setWallet(wallet);
        walletJobEntity.setFrequency(frequency);
        walletJobRepository.save(walletJobEntity);
        return modelMapper.map(wallet, WalletDto.class);
    }

    public WalletDto findWalletById(UUID uuid) {
        WalletEntity wallet = walletRepository.findById(uuid)
            .orElseThrow(() -> new WalletNotFoundException("Wallet with id " + uuid + " not found"));
        return modelMapper.map(wallet, WalletDto.class);
    }
}
