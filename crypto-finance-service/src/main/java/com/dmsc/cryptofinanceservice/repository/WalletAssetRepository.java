package com.dmsc.cryptofinanceservice.repository;

import com.dmsc.cryptofinanceservice.model.entity.WalletAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletAssetRepository extends JpaRepository<WalletAssetEntity, Long> {
}
