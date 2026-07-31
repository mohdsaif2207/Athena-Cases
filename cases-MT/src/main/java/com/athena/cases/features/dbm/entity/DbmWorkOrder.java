package com.athena.cases.features.dbm.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.athena.cases.casemanagement.entity.Case;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * DBM-specific detail row mapped to {@code dbm_work_order} (1:1 with {@link Case}).
 */
@Entity
@Table(name = "dbm_work_order")
public class DbmWorkOrder {

    @Id
    @Column(name = "case_id")
    private Long caseId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "case_id")
    private Case caseEntity;

    @Column(name = "vendor", nullable = false, length = 32)
    private String vendor;

    @Column(name = "core_processor_conversion", nullable = false)
    private boolean coreProcessorConversion;

    @Column(name = "transfer_type", nullable = false, length = 64)
    private String transferType;

    @Column(name = "return_file_expected", nullable = false, length = 8)
    private String returnFileExpected;

    @Column(name = "pgp_key_at_acxiom", length = 8)
    private String pgpKeyAtAcxiom;

    @Column(name = "expected_quantity")
    private Integer expectedQuantity;

    @Column(name = "frequency", length = 32)
    private String frequency;

    @Column(name = "special_instructions")
    private String specialInstructions;

    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "media_ids")
    private String mediaIds;

    @Column(name = "mail_month", length = 32)
    private String mailMonth;

    @Column(name = "media_out_quantity")
    private Integer mediaOutQuantity;

    @Column(name = "changes_to_matchback_db", nullable = false)
    private boolean changesToMatchbackDb;

    @Column(name = "selection_criteria")
    private String selectionCriteria;

    @Column(name = "matchback_field", length = 200)
    private String matchbackField;

    @Column(name = "change_to", length = 200)
    private String changeTo;

    @Column(name = "dbm_work_order_number", length = 64)
    private String dbmWorkOrderNumber;

    @Column(name = "dbm_completion_notes")
    private String dbmCompletionNotes;

    @Column(name = "total_records_updated")
    private Integer totalRecordsUpdated;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;

    @OneToMany(mappedBy = "dbmWorkOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DbmWorkOrderCoverageLevel> coverageLevels = new HashSet<>();

    @OneToMany(mappedBy = "dbmWorkOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DbmWorkOrderAccountType> accountTypes = new HashSet<>();

    @OneToMany(mappedBy = "dbmWorkOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DbmWorkOrderSpokenKey> spokenKeys = new HashSet<>();

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Case getCaseEntity() {
        return caseEntity;
    }

    public void setCaseEntity(Case caseEntity) {
        this.caseEntity = caseEntity;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public boolean isCoreProcessorConversion() {
        return coreProcessorConversion;
    }

    public void setCoreProcessorConversion(boolean coreProcessorConversion) {
        this.coreProcessorConversion = coreProcessorConversion;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getReturnFileExpected() {
        return returnFileExpected;
    }

    public void setReturnFileExpected(String returnFileExpected) {
        this.returnFileExpected = returnFileExpected;
    }

    public String getPgpKeyAtAcxiom() {
        return pgpKeyAtAcxiom;
    }

    public void setPgpKeyAtAcxiom(String pgpKeyAtAcxiom) {
        this.pgpKeyAtAcxiom = pgpKeyAtAcxiom;
    }

    public Integer getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(Integer expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(String mediaIds) {
        this.mediaIds = mediaIds;
    }

    public String getMailMonth() {
        return mailMonth;
    }

    public void setMailMonth(String mailMonth) {
        this.mailMonth = mailMonth;
    }

    public Integer getMediaOutQuantity() {
        return mediaOutQuantity;
    }

    public void setMediaOutQuantity(Integer mediaOutQuantity) {
        this.mediaOutQuantity = mediaOutQuantity;
    }

    public boolean isChangesToMatchbackDb() {
        return changesToMatchbackDb;
    }

    public void setChangesToMatchbackDb(boolean changesToMatchbackDb) {
        this.changesToMatchbackDb = changesToMatchbackDb;
    }

    public String getSelectionCriteria() {
        return selectionCriteria;
    }

    public void setSelectionCriteria(String selectionCriteria) {
        this.selectionCriteria = selectionCriteria;
    }

    public String getMatchbackField() {
        return matchbackField;
    }

    public void setMatchbackField(String matchbackField) {
        this.matchbackField = matchbackField;
    }

    public String getChangeTo() {
        return changeTo;
    }

    public void setChangeTo(String changeTo) {
        this.changeTo = changeTo;
    }

    public String getDbmWorkOrderNumber() {
        return dbmWorkOrderNumber;
    }

    public void setDbmWorkOrderNumber(String dbmWorkOrderNumber) {
        this.dbmWorkOrderNumber = dbmWorkOrderNumber;
    }

    public String getDbmCompletionNotes() {
        return dbmCompletionNotes;
    }

    public void setDbmCompletionNotes(String dbmCompletionNotes) {
        this.dbmCompletionNotes = dbmCompletionNotes;
    }

    public Integer getTotalRecordsUpdated() {
        return totalRecordsUpdated;
    }

    public void setTotalRecordsUpdated(Integer totalRecordsUpdated) {
        this.totalRecordsUpdated = totalRecordsUpdated;
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

    public Set<DbmWorkOrderCoverageLevel> getCoverageLevels() {
        return coverageLevels;
    }

    public void setCoverageLevels(Set<DbmWorkOrderCoverageLevel> coverageLevels) {
        this.coverageLevels = coverageLevels;
    }

    public Set<DbmWorkOrderAccountType> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(Set<DbmWorkOrderAccountType> accountTypes) {
        this.accountTypes = accountTypes;
    }

    public Set<DbmWorkOrderSpokenKey> getSpokenKeys() {
        return spokenKeys;
    }

    public void setSpokenKeys(Set<DbmWorkOrderSpokenKey> spokenKeys) {
        this.spokenKeys = spokenKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DbmWorkOrder other = (DbmWorkOrder) o;
        return caseId != null && Objects.equals(caseId, other.caseId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
