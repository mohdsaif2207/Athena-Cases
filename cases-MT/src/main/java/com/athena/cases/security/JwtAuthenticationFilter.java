package com.athena.cases.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        try {
            Claims claims = jwtService.parseClaims(token);
            Long userId = Long.valueOf(claims.getSubject());
            String username = String.valueOf(claims.get("username"));
            String displayName = String.valueOf(claims.get("displayName"));
            List<String> roles = JwtService.asStringList(claims.get("roles"));
            List<String> permissions = JwtService.asStringList(claims.get("permissions"));
            List<String> groups = JwtService.asStringList(claims.get("groups"));
            List<String> teams = JwtService.asStringList(claims.get("teams"));
            List<String> caseTypes = JwtService.asStringList(claims.get("caseTypes"));
            List<String> initiatingTeams = JwtService.asStringList(claims.get("initiatingTeams"));
            List<String> receivingTeams = JwtService.asStringList(claims.get("receivingTeams"));

            UserPrincipal principal = new UserPrincipal(
                    userId,
                    username,
                    "",
                    displayName,
                    true,
                    roles,
                    permissions,
                    groups,
                    teams,
                    caseTypes,
                    initiatingTeams,
                    receivingTeams);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("invalid jwt - path={} reason={}", request.getRequestURI(), ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
