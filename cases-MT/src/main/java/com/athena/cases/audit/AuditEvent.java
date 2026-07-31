package com.athena.cases.audit;

import java.time.Instant;

/**
 * Audit event payload. Avoid putting secrets or raw PII into {@code detail}.
 */
public record AuditEvent(
        String actorUserId,
        String action,
        String entityType,
        String entityId,
        String detail,
        String requestId,
        Instant occurredAt
) {
}
