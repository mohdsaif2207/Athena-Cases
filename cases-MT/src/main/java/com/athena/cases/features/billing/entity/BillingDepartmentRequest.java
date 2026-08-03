package com.athena.cases.features.billing.entity;

import com.athena.cases.features.billing.enums.BillingExtractType;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.features.billing.enums.BillingRequestType;
import com.athena.cases.features.billing.enums.PreNoteRequestType;
import com.athena.cases.features.billing.enums.PriorHardDeclines;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Billing Department Request extension row — LLD §14.2 / Flyway §13.2.
 *
 * <p>Linked to the shared case header by {@code caseId} only (no Case entity yet).
 * Priority / Status / Case Owner live on {@code cases} — not mapped here.
 *
 * <p>TODO Replace with actual implementation after module integration —
 * Shared {@code com.athena.cases.common.entity.AuditableEntity} exists and is the
 * intended base for Billing entities, but it is not yet a {@code @MappedSuperclass}
 * with JPA id/audit annotations. This class therefore maps id/audit columns locally
 * and does not extend the shared base. Once Lead completes foundation JPA wiring,
 * extend {@code AuditableEntity} and remove the duplicated id/audit fields below.
 * Do not modify shared foundation classes from the Billing module.
 */
@Entity
@Table(name = "billing_department_requests")
public class BillingDepartmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Shared cases.id — Long FK until Case entity is published by Case Management. */
    @Column(name = "case_id", nullable = false, unique = true)
    private Long caseId;

    /**
     * Billing display identifier (BIL######). Not the surrogate PK.
     * Allocated by shared Case Management as {@code cases.case_number}; Billing stores the same
 * value in {@code business_case_id} (no second allocation).
     */
    @Column(name = "business_case_id", nullable = false, unique = true, length = 16)
    private String businessCaseId;

    @Column(name = "request_type", length = 64)
    private BillingRequestType requestType;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "campaign_id", length = 64)
    private String campaignId;

    @Column(name = "reason_for_importance", length = 500)
    private String reasonForImportance;

    @Column(name = "daily_issue_report", nullable = false)
    private boolean dailyIssueReport;

    @Column(name = "approx_number_of_coverages")
    private Integer approxNumberOfCoverages;

    @Column(name = "approx_revenue_impact", precision = 18, scale = 2)
    private BigDecimal approxRevenueImpact;

    @Column(name = "requested_due_date")
    private LocalDate requestedDueDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "segment_id")
    private Long segmentId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "anticipated_release_date")
    private LocalDate anticipatedReleaseDate;

    @Column(name = "request_description", nullable = false, length = 5000)
    private String requestDescription;

    @Column(name = "billing_extract_type", length = 32)
    private BillingExtractType billingExtractType;

    @Column(name = "pre_note_request_type", length = 32)
    private PreNoteRequestType preNoteRequestType;

    @Column(name = "billing_institution", length = 255)
    private String billingInstitution;

    @Column(name = "target_post_date")
    private LocalDate targetPostDate;

    @Column(name = "bill_set", length = 255)
    private String billSet;

    @Column(name = "billing_cycle", length = 255)
    private String billingCycle;

    @Column(name = "prior_hard_declines", length = 8)
    private PriorHardDeclines priorHardDeclines;

    @Column(name = "hard_decline_codes", length = 500)
    private String hardDeclineCodes;

    @Column(name = "billing_hold_type", length = 32)
    private BillingHoldType billingHoldType;

    @Column(name = "hold_reason", length = 1000)
    private String holdReason;

    @Column(name = "billing_hold_by_product_id")
    private Long billingHoldByProductId;

    @OneToMany(mappedBy = "billingRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BillingRequestHoldLevel> holdLevels = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 64)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;

    @Version
    @Column(nullable = false)
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getBusinessCaseId() {
        return businessCaseId;
    }

    public void setBusinessCaseId(String businessCaseId) {
        this.businessCaseId = businessCaseId;
    }

    public BillingRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(BillingRequestType requestType) {
        this.requestType = requestType;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getReasonForImportance() {
        return reasonForImportance;
    }

    public void setReasonForImportance(String reasonForImportance) {
        this.reasonForImportance = reasonForImportance;
    }

    public boolean isDailyIssueReport() {
        return dailyIssueReport;
    }

    public void setDailyIssueReport(boolean dailyIssueReport) {
        this.dailyIssueReport = dailyIssueReport;
    }

    public Integer getApproxNumberOfCoverages() {
        return approxNumberOfCoverages;
    }

    public void setApproxNumberOfCoverages(Integer approxNumberOfCoverages) {
        this.approxNumberOfCoverages = approxNumberOfCoverages;
    }

    public BigDecimal getApproxRevenueImpact() {
        return approxRevenueImpact;
    }

    public void setApproxRevenueImpact(BigDecimal approxRevenueImpact) {
        this.approxRevenueImpact = approxRevenueImpact;
    }

    public LocalDate getRequestedDueDate() {
        return requestedDueDate;
    }

    public void setRequestedDueDate(LocalDate requestedDueDate) {
        this.requestedDueDate = requestedDueDate;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Long getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(Long segmentId) {
        this.segmentId = segmentId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDate getAnticipatedReleaseDate() {
        return anticipatedReleaseDate;
    }

    public void setAnticipatedReleaseDate(LocalDate anticipatedReleaseDate) {
        this.anticipatedReleaseDate = anticipatedReleaseDate;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    public BillingExtractType getBillingExtractType() {
        return billingExtractType;
    }

    public void setBillingExtractType(BillingExtractType billingExtractType) {
        this.billingExtractType = billingExtractType;
    }

    public PreNoteRequestType getPreNoteRequestType() {
        return preNoteRequestType;
    }

    public void setPreNoteRequestType(PreNoteRequestType preNoteRequestType) {
        this.preNoteRequestType = preNoteRequestType;
    }

    public String getBillingInstitution() {
        return billingInstitution;
    }

    public void setBillingInstitution(String billingInstitution) {
        this.billingInstitution = billingInstitution;
    }

    public LocalDate getTargetPostDate() {
        return targetPostDate;
    }

    public void setTargetPostDate(LocalDate targetPostDate) {
        this.targetPostDate = targetPostDate;
    }

    public String getBillSet() {
        return billSet;
    }

    public void setBillSet(String billSet) {
        this.billSet = billSet;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public PriorHardDeclines getPriorHardDeclines() {
        return priorHardDeclines;
    }

    public void setPriorHardDeclines(PriorHardDeclines priorHardDeclines) {
        this.priorHardDeclines = priorHardDeclines;
    }

    public String getHardDeclineCodes() {
        return hardDeclineCodes;
    }

    public void setHardDeclineCodes(String hardDeclineCodes) {
        this.hardDeclineCodes = hardDeclineCodes;
    }

    public BillingHoldType getBillingHoldType() {
        return billingHoldType;
    }

    public void setBillingHoldType(BillingHoldType billingHoldType) {
        this.billingHoldType = billingHoldType;
    }

    public String getHoldReason() {
        return holdReason;
    }

    public void setHoldReason(String holdReason) {
        this.holdReason = holdReason;
    }

    public Long getBillingHoldByProductId() {
        return billingHoldByProductId;
    }

    public void setBillingHoldByProductId(Long billingHoldByProductId) {
        this.billingHoldByProductId = billingHoldByProductId;
    }

    public Set<BillingRequestHoldLevel> getHoldLevels() {
        return holdLevels;
    }

    public void setHoldLevels(Set<BillingRequestHoldLevel> holdLevels) {
        this.holdLevels = holdLevels;
    }

    /** Maintains both sides of the hold-level association. */
    public void addHoldLevel(BillingRequestHoldLevel holdLevel) {
        holdLevels.add(holdLevel);
        holdLevel.setBillingRequest(this);
    }

    public void clearHoldLevels() {
        holdLevels.forEach(hl -> hl.setBillingRequest(null));
        holdLevels.clear();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BillingDepartmentRequest that = (BillingDepartmentRequest) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
