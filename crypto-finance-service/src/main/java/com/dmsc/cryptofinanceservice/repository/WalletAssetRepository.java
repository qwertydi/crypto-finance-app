package com.dmsc.cryptofinanceservice.repository;

import com.dmsc.cryptofinanceservice.model.entity.WalletAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletAssetRepository extends JpaRepository<WalletAssetEntity, Long> {
    List<WalletAssetEntity> findByWalletId(UUID uuid);

    Optional<WalletAssetEntity> findByWalletIdAndId(UUID walletId, Long walletAssetId);
}
