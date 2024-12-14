package com.dmsc.cryptofinanceservice.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity(name = "wallet_assets")
public class WalletAssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;
    /**
     * External ID will be the coincap id field
     */
    private String externalId;
    private String symbol;
    private String name;
    /**
     * Asset quantity in wallet
     */
    private BigDecimal quantity;
    /**
     * Price of the specific asset
     */
    private BigDecimal price;
}
