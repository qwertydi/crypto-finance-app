package com.dmsc.cryptofinanceservice.repository;

import com.dmsc.cryptofinanceservice.model.entity.CryptoPriceEntity;

import java.util.List;

public interface CryptoPriceRepositoryCustom {
    List<CryptoPriceEntity> findDistinctEntities();
}
