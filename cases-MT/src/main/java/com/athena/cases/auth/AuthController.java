package com.athena.cases.auth;

import com.athena.cases.auth.dto.AuthenticatedUserResponse;
import com.athena.cases.auth.dto.LoginRequest;
import com.athena.cases.auth.dto.LoginResponse;
import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.common.web.RequestIdFilter;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user and returns a JWT access token plus profile (including displayName).
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.of(response, currentRequestId()));
    }

    /**
     * Returns the authenticated user's profile from the users table (display_name included).
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthenticatedUserResponse>> me() {
        AuthenticatedUserResponse response = authService.currentUser();
        return ResponseEntity.ok(ApiResponse.of(response, currentRequestId()));
    }

    private static String currentRequestId() {
        String id = MDC.get(RequestIdFilter.MDC_KEY);
        return id == null ? "unknown" : id;
    }
}
