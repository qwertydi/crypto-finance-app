package com.dmsc.cryptofinanceservice.repository;

import com.dmsc.cryptofinanceservice.model.entity.CryptoPriceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface CryptoPriceRepository extends JpaRepository<CryptoPriceEntity, Long>, CryptoPriceRepositoryCustom {
    Optional<CryptoPriceEntity> findTopByExternalIdOrderByTimeDesc(String externalId);

    /**
     * Find the first price for an externalId at the second mark and not full instant
     *
     * @param externalId String
     * @param time       Instant
     * @return Optional<CryptoPriceEntity>
     */
    @Query("SELECT c FROM crypto_price c " +
        "WHERE c.externalId = :externalId " +
        "AND DATE_TRUNC('second', c.time) = DATE_TRUNC('second', CAST(:time AS TIMESTAMP)) " +
        "ORDER BY c.time DESC")
    Page<CryptoPriceEntity> findTopByExternalIdAndTimeOrderByTimeDesc(
        @Param("externalId") String externalId,
        @Param("time") Instant time,
        Pageable pageable
    );
}
