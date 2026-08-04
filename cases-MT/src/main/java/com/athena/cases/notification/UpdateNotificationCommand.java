package com.athena.cases.notification;

/**
 * Partial update for a Notification Queue row (receiving-team editors).
 */
public record UpdateNotificationCommand(
        String messageName,
        String message,
        String details
) {
}
