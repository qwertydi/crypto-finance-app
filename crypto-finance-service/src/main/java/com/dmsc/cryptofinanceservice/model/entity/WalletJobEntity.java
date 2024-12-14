package com.dmsc.cryptofinanceservice.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@AllArgsConstructor
@Data
@Entity
@NoArgsConstructor
public class WalletJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id")
    private WalletEntity wallet;

    private Duration frequency;

    private Instant lastRun;
}
