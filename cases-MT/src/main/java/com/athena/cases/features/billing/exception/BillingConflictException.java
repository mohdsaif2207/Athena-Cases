package com.athena.cases.features.billing.exception;

/**
 * Optimistic lock / state conflict — LLD §20.4 (409).
 * TODO Replace with shared ConflictException after common exception package lands.
 */
public class BillingConflictException extends RuntimeException {

    public BillingConflictException(String message) {
        super(message);
    }
}
