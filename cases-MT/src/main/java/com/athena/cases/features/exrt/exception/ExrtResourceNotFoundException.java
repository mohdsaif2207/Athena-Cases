package com.athena.cases.features.exrt.exception;

public class ExrtResourceNotFoundException extends ExrtBusinessException {

    public ExrtResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
