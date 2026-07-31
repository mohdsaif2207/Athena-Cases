package com.athena.cases.workflow;

import com.athena.cases.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "workflows")
public class WorkflowEntity extends AuditableEntity {

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "receiving_team_id", nullable = false)
    private Long receivingTeamId;

    @Column(name = "message_key", nullable = false, length = 120)
    private String messageKey;

    @Column(name = "message_id", nullable = false, length = 120)
    private String messageId;

    @Column(name = "message_name", nullable = false, length = 255)
    private String messageName;

    @Column(name = "message_object", length = 255)
    private String messageObject;

    @Column(name = "status", nullable = false, length = 64)
    private String status;

    @Column(name = "decision", length = 120)
    private String decision;

    @Column(name = "owner_name", length = 120)
    private String ownerName;

    @Column(name = "priority", nullable = false, length = 32)
    private String priority;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "action_label", length = 64)
    private String actionLabel;

    @Column(name = "logs", length = 4000)
    private String logs;

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getReceivingTeamId() {
        return receivingTeamId;
    }

    public void setReceivingTeamId(Long receivingTeamId) {
        this.receivingTeamId = receivingTeamId;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getMessageObject() {
        return messageObject;
    }

    public void setMessageObject(String messageObject) {
        this.messageObject = messageObject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }
}
