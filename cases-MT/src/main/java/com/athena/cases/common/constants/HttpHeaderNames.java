package com.athena.cases.common.constants;

/**
 * Cross-cutting HTTP / MDC constant names.
 */
public final class HttpHeaderNames {

    public static final String REQUEST_ID = "X-Request-Id";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private HttpHeaderNames() {
    }
}
