package com.athena.cases.features.billing.repository;

import com.athena.cases.features.billing.entity.BillingRequestHoldLevel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Optional hold-level persistence — LLD §18.2.
 *
 * <p>Preferred write path is cascade via {@code BillingDepartmentRequest.holdLevels}.
 * These derived methods support explicit replace/delete and read when needed.
 * No custom {@code @Query} required.
 */
public interface BillingRequestHoldLevelRepository
        extends JpaRepository<BillingRequestHoldLevel, Long> {

    void deleteByBillingRequest_Id(Long billingRequestId);

    List<BillingRequestHoldLevel> findByBillingRequest_Id(Long billingRequestId);
}
