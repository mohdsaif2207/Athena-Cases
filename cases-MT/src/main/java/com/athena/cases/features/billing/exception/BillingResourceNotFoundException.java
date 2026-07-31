package com.athena.cases.features.billing.exception;

/**
 * Billing domain not-found — LLD §26.
 * TODO Replace with shared ResourceNotFoundException after common exception package lands.
 */
public class BillingResourceNotFoundException extends RuntimeException {

    public BillingResourceNotFoundException(String message) {
        super(message);
    }
}
