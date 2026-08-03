package com.athena.cases.features.exrt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "exrt_case_detail")
public class ExrtCaseDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private CaseHeaderEntity caseHeader;

    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Column(name = "mi", length = 1)
    private String mi;

    @Column(name = "state", length = 64)
    private String state;

    @Column(name = "phone_number", length = 10)
    private String phoneNumber;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "coverage_id", length = 64)
    private String coverageId;

    @Column(name = "carrier_id", nullable = false, length = 64)
    private String carrierId;

    @Column(name = "customer_contact_email", length = 255)
    private String customerContactEmail;

    @Column(name = "tier_ii_agent_id", length = 64)
    private String tierIiAgentId;

    @Column(name = "exrt_type", nullable = false, length = 32)
    private String exrtType;

    @Column(name = "policy_number", length = 25)
    private String policyNumber;

    @Column(name = "disposition", nullable = false, length = 128)
    private String disposition;

    @Column(name = "inquiry_source", nullable = false, length = 64)
    private String inquirySource;

    @Column(name = "action_needed", nullable = false, length = 64)
    private String actionNeeded;

    @Column(name = "request_assigned_to", length = 128)
    private String requestAssignedTo;

    @Column(name = "reason_for_escalation", nullable = false, length = 128)
    private String reasonForEscalation;

    @Column(name = "reason_code_1", nullable = false, length = 128)
    private String reasonCode1;

    @Column(name = "requestor_notes", length = 1000)
    private String requestorNotes;

    @Column(name = "notes_issues", length = 1000)
    private String notesIssues;

    @Column(name = "coaching_feedback", length = 1000)
    private String coachingFeedback;

    @Column(name = "call_center_education", nullable = false)
    private boolean callCenterEducation;

    @Column(name = "case_origin", nullable = false, length = 64)
    private String caseOrigin;

    @Column(name = "web_mail", length = 255)
    private String webMail;

    @Column(name = "contact_name", length = 200)
    private String contactName;

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

    public CaseHeaderEntity getCaseHeader() {
        return caseHeader;
    }

    public void setCaseHeader(CaseHeaderEntity caseHeader) {
        this.caseHeader = caseHeader;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMi() {
        return mi;
    }

    public void setMi(String mi) {
        this.mi = mi;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public String getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(String carrierId) {
        this.carrierId = carrierId;
    }

    public String getCustomerContactEmail() {
        return customerContactEmail;
    }

    public void setCustomerContactEmail(String customerContactEmail) {
        this.customerContactEmail = customerContactEmail;
    }

    public String getTierIiAgentId() {
        return tierIiAgentId;
    }

    public void setTierIiAgentId(String tierIiAgentId) {
        this.tierIiAgentId = tierIiAgentId;
    }

    public String getExrtType() {
        return exrtType;
    }

    public void setExrtType(String exrtType) {
        this.exrtType = exrtType;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getInquirySource() {
        return inquirySource;
    }

    public void setInquirySource(String inquirySource) {
        this.inquirySource = inquirySource;
    }

    public String getActionNeeded() {
        return actionNeeded;
    }

    public void setActionNeeded(String actionNeeded) {
        this.actionNeeded = actionNeeded;
    }

    public String getRequestAssignedTo() {
        return requestAssignedTo;
    }

    public void setRequestAssignedTo(String requestAssignedTo) {
        this.requestAssignedTo = requestAssignedTo;
    }

    public String getReasonForEscalation() {
        return reasonForEscalation;
    }

    public void setReasonForEscalation(String reasonForEscalation) {
        this.reasonForEscalation = reasonForEscalation;
    }

    public String getReasonCode1() {
        return reasonCode1;
    }

    public void setReasonCode1(String reasonCode1) {
        this.reasonCode1 = reasonCode1;
    }

    public String getRequestorNotes() {
        return requestorNotes;
    }

    public void setRequestorNotes(String requestorNotes) {
        this.requestorNotes = requestorNotes;
    }

    public String getNotesIssues() {
        return notesIssues;
    }

    public void setNotesIssues(String notesIssues) {
        this.notesIssues = notesIssues;
    }

    public String getCoachingFeedback() {
        return coachingFeedback;
    }

    public void setCoachingFeedback(String coachingFeedback) {
        this.coachingFeedback = coachingFeedback;
    }

    public boolean isCallCenterEducation() {
        return callCenterEducation;
    }

    public void setCallCenterEducation(boolean callCenterEducation) {
        this.callCenterEducation = callCenterEducation;
    }

    public String getCaseOrigin() {
        return caseOrigin;
    }

    public void setCaseOrigin(String caseOrigin) {
        this.caseOrigin = caseOrigin;
    }

    public String getWebMail() {
        return webMail;
    }

    public void setWebMail(String webMail) {
        this.webMail = webMail;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
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
}
