package com.athena.cases.common.exception;

/**
 * Thrown when credentials are invalid or the account is not eligible to sign in.
 */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
