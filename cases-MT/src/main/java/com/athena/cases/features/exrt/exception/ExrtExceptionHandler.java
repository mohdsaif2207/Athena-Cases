package com.athena.cases.features.exrt.exception;

import com.athena.cases.common.dto.ErrorResponse;
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

@RestControllerAdvice(basePackages = "com.athena.cases.features.exrt")
public class ExrtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ExrtExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();
        String requestId = requestId(request);
        return ResponseEntity.badRequest().body(ErrorResponse.of(
                "VALIDATION_ERROR",
                "Invalid request",
                details.isEmpty() ? null : details.getFirst().field(),
                requestId,
                details
        ));
    }

    @ExceptionHandler(ExrtResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ExrtResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("exrt resource not found - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
                ex.getCode(), ex.getMessage(), ex.getField(), requestId(request), List.of()
        ));
    }

    @ExceptionHandler(ExrtBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(ExrtBusinessException ex, HttpServletRequest request) {
        HttpStatus status = "FORBIDDEN".equals(ex.getCode()) ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
        log.warn("exrt business error - code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(status).body(ErrorResponse.of(
                ex.getCode(), ex.getMessage(), ex.getField(), requestId(request), List.of()
        ));
    }

    private ErrorResponse.FieldErrorDetail toDetail(FieldError error) {
        return new ErrorResponse.FieldErrorDetail(error.getField(), error.getDefaultMessage(), "FIELD_INVALID");
    }

    private static String requestId(HttpServletRequest request) {
        String header = request.getHeader("X-Request-Id");
        return header == null || header.isBlank() ? UUID.randomUUID().toString() : header;
    }
}
