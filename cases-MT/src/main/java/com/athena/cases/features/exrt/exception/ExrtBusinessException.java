package com.athena.cases.features.exrt.exception;

public class ExrtBusinessException extends RuntimeException {

    private final String code;
    private final String field;

    public ExrtBusinessException(String code, String message) {
        this(code, message, null);
    }

    public ExrtBusinessException(String code, String message, String field) {
        super(message);
        this.code = code;
        this.field = field;
    }

    public String getCode() {
        return code;
    }

    public String getField() {
        return field;
    }
}
