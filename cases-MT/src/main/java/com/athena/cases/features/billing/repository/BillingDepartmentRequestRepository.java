package com.athena.cases.features.billing.repository;

import com.athena.cases.features.billing.entity.BillingDepartmentRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for Billing Department Request — LLD §18.1 / §18.4.
 *
 * <p>LLD names methods {@code findByCaseEntity_Id} / {@code existsByCaseEntity_Id}
 * for a {@code Case} association. Billing maps {@code Long caseId} only; method
 * names follow the {@code caseId} property.
 */
public interface BillingDepartmentRequestRepository
        extends JpaRepository<BillingDepartmentRequest, Long> {

    /**
     * Load billing extension for View/Edit by shared case id, including hold levels
     * in one fetch (LLD §18.4 — avoid N+1 on View).
     */
    @EntityGraph(attributePaths = "holdLevels")
    Optional<BillingDepartmentRequest> findByCaseId(Long caseId);

    /**
     * Guard against a duplicate billing extension for the same case.
     */
    boolean existsByCaseId(Long caseId);
}
