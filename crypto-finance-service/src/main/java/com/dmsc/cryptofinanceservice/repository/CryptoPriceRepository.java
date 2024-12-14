package com.dmsc.cryptofinanceservice.repository;

import com.dmsc.cryptofinanceservice.model.entity.CryptoPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CryptoPriceRepository extends JpaRepository<CryptoPriceEntity, Long> {
}
