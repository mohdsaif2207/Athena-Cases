package com.athena.cases.features.exrt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "case_header")
public class CaseHeaderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", nullable = false, length = 64, unique = true)
    private String caseNumber;

    @Column(name = "case_type", nullable = false, length = 64)
    private String caseType;

    @Column(name = "client_id", length = 64)
    private String clientId;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "case_owner", nullable = false, length = 128)
    private String caseOwner;

    @Column(name = "status", nullable = false, length = 64)
    private String status;

    @Column(name = "priority", nullable = false, length = 32)
    private String priority;

    @Column(name = "requested_due_date")
    private LocalDate requestedDueDate;

    @Column(name = "workflow_triggered", nullable = false)
    private boolean workflowTriggered;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @OneToOne(mappedBy = "caseHeader")
    private ExrtCaseDetailEntity exrtDetail;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (version == null) {
            version = 1;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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

    public String getCaseOwner() {
        return caseOwner;
    }

    public void setCaseOwner(String caseOwner) {
        this.caseOwner = caseOwner;
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

    public LocalDate getRequestedDueDate() {
        return requestedDueDate;
    }

    public void setRequestedDueDate(LocalDate requestedDueDate) {
        this.requestedDueDate = requestedDueDate;
    }

    public boolean isWorkflowTriggered() {
        return workflowTriggered;
    }

    public void setWorkflowTriggered(boolean workflowTriggered) {
        this.workflowTriggered = workflowTriggered;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ExrtCaseDetailEntity getExrtDetail() {
        return exrtDetail;
    }

    public void setExrtDetail(ExrtCaseDetailEntity exrtDetail) {
        this.exrtDetail = exrtDetail;
    }
}
