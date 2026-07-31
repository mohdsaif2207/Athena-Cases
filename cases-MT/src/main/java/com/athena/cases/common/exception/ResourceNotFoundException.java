package com.athena.cases.common.exception;

/**
 * Thrown when a requested domain resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String type, String id) {
        super("%s not found: %s".formatted(type, id));
    }
}
