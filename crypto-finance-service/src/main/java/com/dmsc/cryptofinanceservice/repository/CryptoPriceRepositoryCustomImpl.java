package com.dmsc.cryptofinanceservice.repository;

import com.dmsc.cryptofinanceservice.model.entity.CryptoPriceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoPriceRepositoryCustomImpl implements CryptoPriceRepositoryCustom {

    private final EntityManager entityManager;

    public CryptoPriceRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Find all distinct entities for {@link CryptoPriceEntity}
     * This method demonstrates the use of the Criteria API to query the {@link CryptoPriceEntity} database table.
     * It selects distinct {@link CryptoPriceEntity} rows based on the fields {@link CryptoPriceEntity#getExternalId()}, {@link CryptoPriceEntity#getName()}, and {@link CryptoPriceEntity#getSymbol()},
     * while ensuring these fields are non-null.
     *
     * @return List<CryptoPriceEntity>
     */
    public List<CryptoPriceEntity> findDistinctEntities() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CryptoPriceEntity> cq = cb.createQuery(CryptoPriceEntity.class);
        Root<CryptoPriceEntity> root = cq.from(CryptoPriceEntity.class);

        // Filter non-null fields before performing group by
        cq.where(
            cb.isNotNull(root.get("externalId")),
            cb.isNotNull(root.get("name")),
            cb.isNotNull(root.get("symbol"))
        );

        // Subquery to get the minimum ID for each distinct externalId, name, and symbol
        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<CryptoPriceEntity> subRoot = subquery.from(CryptoPriceEntity.class);

        // Group by externalId, name, and symbol in the subquery
        subquery.select(cb.min(subRoot.get("id")));
        subquery.groupBy(subRoot.get("externalId"), subRoot.get("name"), subRoot.get("symbol"));

        // Add condition to the main query: only select those entities where the ID is in the subquery result
        cq.select(root).where(cb.in(root.get("id")).value(subquery));

        return entityManager.createQuery(cq).getResultList();
    }
}
