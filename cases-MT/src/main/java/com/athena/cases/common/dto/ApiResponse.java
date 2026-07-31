package com.athena.cases.common.dto;

import java.time.Instant;

/**
 * Standard success envelope for API responses.
 *
 * @param data      payload
 * @param requestId correlation id
 * @param timestamp response time (UTC)
 */
public record ApiResponse<T>(T data, String requestId, Instant timestamp) {

    public static <T> ApiResponse<T> of(T data, String requestId) {
        return new ApiResponse<>(data, requestId, Instant.now());
    }
}
