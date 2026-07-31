package com.athena.cases.auth;

import com.athena.cases.auth.dto.AuthenticatedUserResponse;
import com.athena.cases.auth.dto.LoginRequest;
import com.athena.cases.auth.dto.LoginResponse;
import com.athena.cases.common.exception.AuthenticationFailedException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.identity.entity.UserEntity;
import com.athena.cases.identity.repository.UserRepository;
import com.athena.cases.security.DatabaseUserDetailsService;
import com.athena.cases.security.JwtService;
import com.athena.cases.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final DatabaseUserDetailsService userDetailsService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            DatabaseUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username().trim(),
                            request.password()));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);

            log.info(
                    "user logged in - userId={} username={} displayName={} roles={} permissions={}",
                    principal.getUserId(),
                    principal.getUsername(),
                    principal.getDisplayName(),
                    principal.getRoleCodes().size(),
                    principal.getPermissionCodes().size());

            return new LoginResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationMinutes(),
                    toAuthenticatedUser(principal));
        } catch (BadCredentialsException ex) {
            log.warn("login failed - invalid credentials username={}", request.username());
            throw new AuthenticationFailedException("Invalid username or password.");
        } catch (DisabledException | LockedException ex) {
            log.warn("login failed - inactive account username={}", request.username());
            throw new AuthenticationFailedException("Your account is inactive. Please contact an administrator.");
        }
    }

    /**
     * Reloads the authenticated profile from the database so Admin IAM changes apply on session refresh.
     */
    @Transactional(readOnly = true)
    public AuthenticatedUserResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AuthenticationFailedException("Authentication required.");
        }

        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(principal.getUserId())));

        UserPrincipal fresh = userDetailsService.toPrincipal(user);
        return toAuthenticatedUserResponse(fresh);
    }

    private static LoginResponse.AuthenticatedUser toAuthenticatedUser(UserPrincipal principal) {
        return new LoginResponse.AuthenticatedUser(
                principal.getUserId(),
                principal.getUsername(),
                principal.getDisplayName(),
                principal.getRoleCodes(),
                principal.getPermissionCodes(),
                principal.getGroupCodes(),
                principal.getTeamCodes(),
                principal.getCaseTypeCodes(),
                principal.getInitiatingTeamCodes(),
                principal.getReceivingTeamCodes());
    }

    private static AuthenticatedUserResponse toAuthenticatedUserResponse(UserPrincipal principal) {
        return new AuthenticatedUserResponse(
                principal.getUserId(),
                principal.getUsername(),
                principal.getDisplayName(),
                principal.getRoleCodes(),
                principal.getPermissionCodes(),
                principal.getGroupCodes(),
                principal.getTeamCodes(),
                principal.getCaseTypeCodes(),
                principal.getInitiatingTeamCodes(),
                principal.getReceivingTeamCodes());
    }
}
