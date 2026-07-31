package com.athena.cases.common.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard paged success envelope.
 */
public record PageResponse<T>(
        List<T> data,
        PageMeta meta,
        String requestId,
        Instant timestamp
) {

    public static <T> PageResponse<T> of(List<T> data, PageMeta meta, String requestId) {
        return new PageResponse<>(data, meta, requestId, Instant.now());
    }
}
