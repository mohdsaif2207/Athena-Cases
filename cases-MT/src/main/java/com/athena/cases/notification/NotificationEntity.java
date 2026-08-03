package com.athena.cases.notification;

import com.athena.cases.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class NotificationEntity extends AuditableEntity {

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

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "details", length = 4000)
    private String details;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
