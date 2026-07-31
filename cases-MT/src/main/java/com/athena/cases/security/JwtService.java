package com.athena.cases.security;

import com.athena.cases.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getExpirationMinutes() * 60);
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(principal.getUserId()))
                .claim("username", principal.getUsername())
                .claim("displayName", principal.getDisplayName())
                .claim("roles", principal.getRoleCodes())
                .claim("permissions", principal.getPermissionCodes())
                .claim("groups", principal.getGroupCodes())
                .claim("teams", principal.getTeamCodes())
                .claim("caseTypes", principal.getCaseTypeCodes())
                .claim("initiatingTeams", principal.getInitiatingTeamCodes())
                .claim("receivingTeams", principal.getReceivingTeamCodes())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMinutes() {
        return properties.getExpirationMinutes();
    }

    @SuppressWarnings("unchecked")
    public static List<String> asStringList(Object claim) {
        if (claim instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
