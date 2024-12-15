package com.dmsc.cryptofinanceservice.repository;

import com.dmsc.cryptofinanceservice.model.entity.CryptoPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface CryptoPriceRepository extends JpaRepository<CryptoPriceEntity, Long>, CryptoPriceRepositoryCustom {
    Optional<CryptoPriceEntity> findTopByExternalIdOrderByTimeDesc(String externalId);

    Optional<CryptoPriceEntity> findTopByExternalIdAndTimeOrderByTimeDesc(String externalId, Instant time);
}
