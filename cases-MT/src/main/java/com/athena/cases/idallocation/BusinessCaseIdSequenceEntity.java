package com.athena.cases.idallocation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "business_case_id_sequences")
public class BusinessCaseIdSequenceEntity {

    @Id
    @Column(name = "case_type_code", nullable = false, length = 64)
    private String caseTypeCode;

    @Column(name = "prefix", nullable = false, length = 8)
    private String prefix;

    @Column(name = "width", nullable = false)
    private int width;

    @Column(name = "next_value", nullable = false)
    private long nextValue;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getCaseTypeCode() {
        return caseTypeCode;
    }

    public void setCaseTypeCode(String caseTypeCode) {
        this.caseTypeCode = caseTypeCode;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public long getNextValue() {
        return nextValue;
    }

    public void setNextValue(long nextValue) {
        this.nextValue = nextValue;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
