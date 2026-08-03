package com.athena.cases.admin;

import com.athena.cases.admin.dto.CaseTypeAdminItem;
import com.athena.cases.admin.dto.CaseTypeUpsertRequest;
import com.athena.cases.admin.dto.GroupAdminItem;
import com.athena.cases.admin.dto.GroupUpsertRequest;
import com.athena.cases.admin.dto.PermissionItem;
import com.athena.cases.admin.dto.ResetPasswordRequest;
import com.athena.cases.admin.dto.RoleAdminItem;
import com.athena.cases.admin.dto.RoleUpsertRequest;
import com.athena.cases.admin.dto.TeamAdminItem;
import com.athena.cases.admin.dto.TeamUpsertRequest;
import com.athena.cases.admin.dto.UserAdminItem;
import com.athena.cases.admin.dto.UserUpsertRequest;
import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.common.web.RequestIdFilter;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("@adminAccess.allow()")
public class AdminIamController {

    private final AdminIamService adminIamService;

    public AdminIamController(AdminIamService adminIamService) {
        this.adminIamService = adminIamService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserAdminItem>>> listUsers() {
        return ok(adminIamService.listUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserAdminItem>> createUser(@Valid @RequestBody UserUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(adminIamService.createUser(request)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserAdminItem>> updateUser(
            @PathVariable Long id, @Valid @RequestBody UserUpsertRequest request) {
        return ok(adminIamService.updateUser(id, request));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserAdminItem>> setStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return ok(adminIamService.setUserStatus(id, body.get("status")));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(
            @PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        adminIamService.resetPassword(id, request);
        return ok(Map.of("status", "OK"));
    }

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<GroupAdminItem>>> listGroups() {
        return ok(adminIamService.listGroups());
    }

    @PostMapping("/groups")
    public ResponseEntity<ApiResponse<GroupAdminItem>> createGroup(@Valid @RequestBody GroupUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(adminIamService.createGroup(request)));
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<ApiResponse<GroupAdminItem>> updateGroup(
            @PathVariable Long id, @Valid @RequestBody GroupUpsertRequest request) {
        return ok(adminIamService.updateGroup(id, request));
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteGroup(@PathVariable Long id) {
        adminIamService.deleteGroup(id);
        return ok(Map.of("status", "OK"));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleAdminItem>>> listRoles() {
        return ok(adminIamService.listRoles());
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<RoleAdminItem>> createRole(@Valid @RequestBody RoleUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(adminIamService.createRole(request)));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<RoleAdminItem>> updateRole(
            @PathVariable Long id, @Valid @RequestBody RoleUpsertRequest request) {
        return ok(adminIamService.updateRole(id, request));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteRole(@PathVariable Long id) {
        adminIamService.deleteRole(id);
        return ok(Map.of("status", "OK"));
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<PermissionItem>>> listPermissions() {
        return ok(adminIamService.listPermissions());
    }

    @GetMapping("/case-types")
    public ResponseEntity<ApiResponse<List<CaseTypeAdminItem>>> listCaseTypes() {
        return ok(adminIamService.listCaseTypes());
    }

    @PostMapping("/case-types")
    public ResponseEntity<ApiResponse<CaseTypeAdminItem>> createCaseType(
            @Valid @RequestBody CaseTypeUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(adminIamService.createCaseType(request)));
    }

    @PutMapping("/case-types/{id}")
    public ResponseEntity<ApiResponse<CaseTypeAdminItem>> updateCaseType(
            @PathVariable Long id, @Valid @RequestBody CaseTypeUpsertRequest request) {
        return ok(adminIamService.updateCaseType(id, request));
    }

    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<List<TeamAdminItem>>> listTeams() {
        return ok(adminIamService.listTeams());
    }

    @PostMapping("/teams")
    public ResponseEntity<ApiResponse<TeamAdminItem>> createTeam(@Valid @RequestBody TeamUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(adminIamService.createTeam(request)));
    }

    @PutMapping("/teams/{id}")
    public ResponseEntity<ApiResponse<TeamAdminItem>> updateTeam(
            @PathVariable Long id, @Valid @RequestBody TeamUpsertRequest request) {
        return ok(adminIamService.updateTeam(id, request));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(wrap(data));
    }

    private <T> ApiResponse<T> wrap(T data) {
        return ApiResponse.of(data, currentRequestId());
    }

    private static String currentRequestId() {
        String id = MDC.get(RequestIdFilter.MDC_KEY);
        return id == null ? "unknown" : id;
    }
}
