package com.athena.cases.features.billing.exception;

/**
 * Billing field / referential validation failure — LLD §20.
 * TODO Replace with shared ValidationException after common exception package lands.
 */
public class BillingValidationException extends RuntimeException {

    private final String field;

    public BillingValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
