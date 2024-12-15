package com.dmsc.cryptofinanceservice.repository;

import com.dmsc.cryptofinanceservice.model.entity.WalletJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletJobRepository extends JpaRepository<WalletJobEntity, Long> {
    Optional<WalletJobEntity> findByWalletId(UUID id);
}
