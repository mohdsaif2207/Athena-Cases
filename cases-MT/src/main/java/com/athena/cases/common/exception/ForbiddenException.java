package com.athena.cases.common.exception;

/**
 * Thrown when the caller lacks permission for an operation.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
