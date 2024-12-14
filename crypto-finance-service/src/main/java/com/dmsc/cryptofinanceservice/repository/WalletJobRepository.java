package com.dmsc.cryptofinanceservice.repository;

import com.dmsc.cryptofinanceservice.model.entity.WalletJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletJobRepository extends JpaRepository<WalletJobEntity, Long> {
}
