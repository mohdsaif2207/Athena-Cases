package com.athena.cases.notification;

import java.time.Instant;

/**
 * Notification Queue grid row.
 */
public record NotificationQueueItem(
        Long id,
        String notificationId,
        Long caseId,
        String messageKey,
        String messageId,
        String messageName,
        String messageObject,
        String message,
        Instant receivedAt,
        String details
) {
}
