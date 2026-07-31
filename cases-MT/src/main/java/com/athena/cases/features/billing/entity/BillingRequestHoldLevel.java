package com.athena.cases.features.billing.entity;

import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;

/**
 * Selected Billing Hold Level row — LLD §14.3 / Flyway §13.3.
 *
 * <p>Stores dual-listbox Selected values (join-style; never CSV).
 * Table has {@code created_at} only (no full audit columns per LLD).
 *
 * <p>TODO Replace with actual implementation after module integration —
 * Shared {@code com.athena.cases.common.entity.BaseEntity} exists and is the
 * intended identity base, but it is not yet a {@code @MappedSuperclass} with
 * {@code @Id}. This class maps {@code id} locally until Lead completes foundation
 * JPA wiring. Do not modify shared foundation classes from the Billing module.
 */
@Entity
@Table(
        name = "billing_request_hold_levels",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_billing_request_hold_levels_req_code",
                columnNames = {"billing_request_id", "hold_level_code"}
        )
)
public class BillingRequestHoldLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billing_request_id", nullable = false)
    private BillingDepartmentRequest billingRequest;

    @Column(name = "hold_level_code", nullable = false, length = 32)
    private BillingHoldLevelCode holdLevelCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BillingDepartmentRequest getBillingRequest() {
        return billingRequest;
    }

    public void setBillingRequest(BillingDepartmentRequest billingRequest) {
        this.billingRequest = billingRequest;
    }

    public BillingHoldLevelCode getHoldLevelCode() {
        return holdLevelCode;
    }

    public void setHoldLevelCode(BillingHoldLevelCode holdLevelCode) {
        this.holdLevelCode = holdLevelCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BillingRequestHoldLevel that = (BillingRequestHoldLevel) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
