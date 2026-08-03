package com.athena.cases.features.exrt.controller;

import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.features.exrt.constant.ExrtConstants;
import com.athena.cases.features.exrt.dto.ExrtCaseCreateRequest;
import com.athena.cases.features.exrt.dto.ExrtCaseCreateResponse;
import com.athena.cases.features.exrt.dto.ExrtCaseDetailsResponse;
import com.athena.cases.features.exrt.service.ExrtCaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ExrtConstants.API_BASE)
public class ExrtCaseController {

    private static final Logger log = LoggerFactory.getLogger(ExrtCaseController.class);

    private final ExrtCaseService exrtCaseService;

    public ExrtCaseController(ExrtCaseService exrtCaseService) {
        this.exrtCaseService = exrtCaseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExrtCaseCreateResponse>> create(
            @Valid @RequestBody ExrtCaseCreateRequest request,
            HttpServletRequest httpRequest) {
        String requestId = resolveRequestId(httpRequest);
        log.info("create exrt case request - requestId={}", requestId);
        ExrtCaseCreateResponse created = exrtCaseService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(created, requestId));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<ApiResponse<ExrtCaseDetailsResponse>> getById(
            @PathVariable Long caseId,
            HttpServletRequest httpRequest) {
        String requestId = resolveRequestId(httpRequest);
        return ResponseEntity.ok(ApiResponse.of(exrtCaseService.getById(caseId), requestId));
    }

    private static String resolveRequestId(HttpServletRequest request) {
        String header = request.getHeader("X-Request-Id");
        return header == null || header.isBlank() ? UUID.randomUUID().toString() : header;
    }
}
