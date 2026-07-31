package com.athena.cases.audit;

/**
 * Application-level audit trail (distinct from JPA {@code createdAt}/{@code updatedAt} columns).
 */
public interface AuditService {

    void record(AuditEvent event);
}
