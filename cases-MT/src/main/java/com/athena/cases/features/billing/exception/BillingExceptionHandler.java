package com.athena.cases.features.billing.exception;

import com.athena.cases.common.constants.HttpHeaderNames;
import com.athena.cases.common.dto.ErrorResponse;
import com.athena.cases.features.billing.controller.BillingDepartmentRequestController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Billing-scoped exception → HTTP mapping — LLD §26.
 *
 * <p>No shared {@code GlobalExceptionHandler} exists in source yet (docs list it as Done).
 * This advice is limited to the Billing controller so it does not replace Lead-owned
 * global handling. Remove or narrow further once {@code common.exception.GlobalExceptionHandler}
 * is delivered.
 *
 * <p>TODO Replace with actual implementation after module integration —
 * Prefer shared GlobalExceptionHandler for MethodArgumentNotValidException and security errors.
 */
@RestControllerAdvice(assignableTypes = BillingDepartmentRequestController.class)
public class BillingExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BillingExceptionHandler.class);

    @ExceptionHandler(BillingValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            BillingValidationException ex,
            HttpServletRequest request
    ) {
        log.warn("billing validation failed - field={} message={}", ex.getField(), ex.getMessage());
        String requestId = resolveRequestId(request);
        List<ErrorResponse.FieldErrorDetail> details = List.of(
                new ErrorResponse.FieldErrorDetail(ex.getField(), ex.getMessage(), "VALIDATION_ERROR")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "VALIDATION_ERROR",
                        ex.getMessage(),
                        ex.getField(),
                        requestId,
                        details
                ));
    }

    @ExceptionHandler(BillingResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            BillingResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("billing resource not found - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        "RESOURCE_NOT_FOUND",
                        ex.getMessage(),
                        null,
                        resolveRequestId(request),
                        List.of()
                ));
    }

    @ExceptionHandler(BillingConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            BillingConflictException ex,
            HttpServletRequest request
    ) {
        log.warn("billing conflict - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        "OPTIMISTIC_LOCK_CONFLICT",
                        ex.getMessage(),
                        null,
                        resolveRequestId(request),
                        List.of()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();
        String primaryField = details.isEmpty() ? null : details.get(0).field();
        String message = details.isEmpty() ? "Invalid request" : details.get(0).message();
        log.warn("billing bean validation failed - fields={}", details.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "VALIDATION_ERROR",
                        message,
                        primaryField,
                        resolveRequestId(request),
                        details
                ));
    }

    private ErrorResponse.FieldErrorDetail toDetail(FieldError error) {
        return new ErrorResponse.FieldErrorDetail(
                error.getField(),
                error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage(),
                "VALIDATION_ERROR"
        );
    }

    private static String resolveRequestId(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaderNames.REQUEST_ID);
        if (header == null || header.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return header;
    }
}
