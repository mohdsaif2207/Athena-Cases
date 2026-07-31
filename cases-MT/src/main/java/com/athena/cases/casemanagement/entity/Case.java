package com.athena.cases.casemanagement.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Shared case header mapped to {@code cases}.
 * Business Case ID is {@code caseNumber} (e.g. DBM000001); {@code id} is the internal PK.
 *
 * <p>The DBM 1:1 link is owned by {@code DbmWorkOrder} (feature package) so this shared
 * entity does not depend on a feature module.
 */
@Entity
@Table(
        name = "cases",
        uniqueConstraints = @UniqueConstraint(name = "uq_cases_case_number", columnNames = "case_number")
)
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", nullable = false, length = 32)
    private String caseNumber;

    @Column(name = "case_type", nullable = false, length = 64)
    private String caseType;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status", nullable = false, length = 64)
    private String status;

    @Column(name = "priority", nullable = false, length = 16)
    private String priority;

    @Column(name = "case_owner", nullable = false, length = 120)
    private String caseOwner;

    @Column(name = "requested_due_date", nullable = false)
    private LocalDate requestedDueDate;

    @Column(name = "client_id", length = 64)
    private String clientId;

    @Column(name = "pending_dbm_approval", nullable = false)
    private boolean pendingDbmApproval;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCaseOwner() {
        return caseOwner;
    }

    public void setCaseOwner(String caseOwner) {
        this.caseOwner = caseOwner;
    }

    public LocalDate getRequestedDueDate() {
        return requestedDueDate;
    }

    public void setRequestedDueDate(LocalDate requestedDueDate) {
        this.requestedDueDate = requestedDueDate;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isPendingDbmApproval() {
        return pendingDbmApproval;
    }

    public void setPendingDbmApproval(boolean pendingDbmApproval) {
        this.pendingDbmApproval = pendingDbmApproval;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Case other = (Case) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
