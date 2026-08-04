package com.athena.cases.notification;

import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.common.web.RequestIdFilter;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Lists Notification Queue rows for the authenticated receiving-team principal.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PERM_NOTIF_VIEW')")
    public ResponseEntity<ApiResponse<List<NotificationQueueItem>>> list() {
        List<NotificationQueueItem> rows = notificationService.listAuthorizedQueue();
        return ResponseEntity.ok(ApiResponse.of(rows, currentRequestId()));
    }

    /**
     * Updates message fields for a notification queue row.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_NOTIF_VIEW')")
    public ResponseEntity<ApiResponse<NotificationQueueItem>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotificationCommand command) {
        NotificationQueueItem updated = notificationService.updateQueueItem(id, command);
        return ResponseEntity.ok(ApiResponse.of(updated, currentRequestId()));
    }

    private static String currentRequestId() {
        String id = MDC.get(RequestIdFilter.MDC_KEY);
        return id == null ? "unknown" : id;
    }
}
