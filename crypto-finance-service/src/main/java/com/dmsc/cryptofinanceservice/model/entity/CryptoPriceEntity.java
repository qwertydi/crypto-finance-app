package com.dmsc.cryptofinanceservice.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity(name = "crypto_price")
public class CryptoPriceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String externalId;
    /**
     * Probably duplicated data, can be normalized
     */
    private String name;
    private String symbol;
    private BigDecimal price;
    private Instant time;
}
