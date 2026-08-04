package com.athena.cases.preference;

import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.common.web.RequestIdFilter;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated user's UI preferences (persisted to profile).
 */
@RestController
@RequestMapping("/api/v1/users/me/preferences")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping("/{key}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> get(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.of(userPreferenceService.getMine(key), currentRequestId()));
    }

    @PutMapping("/{key}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> upsert(
            @PathVariable String key,
            @Valid @RequestBody UpsertUserPreferenceCommand command) {
        UserPreferenceResponse saved = userPreferenceService.upsertMine(key, command);
        return ResponseEntity.ok(ApiResponse.of(saved, currentRequestId()));
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        userPreferenceService.deleteMine(key);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private static String currentRequestId() {
        String id = MDC.get(RequestIdFilter.MDC_KEY);
        return id == null ? "unknown" : id;
    }
}
