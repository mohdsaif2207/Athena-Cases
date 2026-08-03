package com.athena.cases.workflow;

import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.common.web.RequestIdFilter;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Lists Workflow Queue rows for the authenticated receiving-team principal.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PERM_WF_VIEW')")
    public ResponseEntity<ApiResponse<List<WorkflowQueueItem>>> list() {
        List<WorkflowQueueItem> rows = workflowService.listAuthorizedQueue();
        return ResponseEntity.ok(ApiResponse.of(rows, currentRequestId()));
    }

    private static String currentRequestId() {
        String id = MDC.get(RequestIdFilter.MDC_KEY);
        return id == null ? "unknown" : id;
    }
}
