package com.athena.cases.common.web;

import com.athena.cases.common.dto.ApiResponse;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        String requestId = MDC.get(RequestIdFilter.MDC_KEY);
        return ResponseEntity.ok(ApiResponse.of(Map.of("status", "UP"), requestId == null ? "unknown" : requestId));
    }
}
