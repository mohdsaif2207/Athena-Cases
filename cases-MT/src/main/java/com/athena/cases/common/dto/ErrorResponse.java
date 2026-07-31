package com.athena.cases.common.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard error envelope matching API standards.
 */
public record ErrorResponse(ErrorBody error) {

    public record ErrorBody(
            String code,
            String message,
            String field,
            String requestId,
            Instant timestamp,
            List<FieldErrorDetail> details
    ) {
    }

    public record FieldErrorDetail(String field, String message, String code) {
    }

    public static ErrorResponse of(String code, String message, String field, String requestId,
                                   List<FieldErrorDetail> details) {
        return new ErrorResponse(new ErrorBody(
                code, message, field, requestId, Instant.now(),
                details == null ? List.of() : details));
    }
}
