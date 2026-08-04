package com.athena.cases.casemanagement;

import com.athena.cases.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "cases")
public class CaseEntity extends AuditableEntity {

    @Column(name = "case_number", nullable = false, unique = true, length = 64)
    private String caseNumber;

    @Column(name = "case_type_id", nullable = false)
    private Long caseTypeId;

    @Column(name = "client_id", length = 64)
    private String clientId;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "case_owner", nullable = false, length = 120)
    private String caseOwner;

    @Column(name = "case_status", nullable = false, length = 64)
    private String caseStatus;

    @Column(name = "priority", nullable = false, length = 32)
    private String priority;

    @Column(name = "carrier", length = 120)
    private String carrier;

    @Column(name = "assigned_to", length = 120)
    private String assignedTo;

    @Column(name = "segment_id", length = 64)
    private String segmentId;

    @Column(name = "frequency", length = 64)
    private String frequency;

    @Column(name = "spoken_key", length = 120)
    private String spokenKey;

    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "mail_month", length = 32)
    private String mailMonth;

    @Column(name = "requested_due_date")
    private LocalDate requestedDueDate;

    /** DBM Custom transfer type — requires DBM manager approval (LLD). */
    @Column(name = "pending_dbm_approval", nullable = false)
    private boolean pendingDbmApproval;

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Long getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(Long caseTypeId) {
        this.caseTypeId = caseTypeId;
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

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getSpokenKey() {
        return spokenKey;
    }

    public void setSpokenKey(String spokenKey) {
        this.spokenKey = spokenKey;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getMailMonth() {
        return mailMonth;
    }

    public void setMailMonth(String mailMonth) {
        this.mailMonth = mailMonth;
    }

    public LocalDate getRequestedDueDate() {
        return requestedDueDate;
    }

    public void setRequestedDueDate(LocalDate requestedDueDate) {
        this.requestedDueDate = requestedDueDate;
    }

    public boolean isPendingDbmApproval() {
        return pendingDbmApproval;
    }

    public void setPendingDbmApproval(boolean pendingDbmApproval) {
        this.pendingDbmApproval = pendingDbmApproval;
    }
}
