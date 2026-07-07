package com.fts.tenantbasededuportal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final String EMAIL = "email";
    private static final String ROLE = "role";
    private static final String ORGANIZATION_ID = "organizationId";


    public String generateToken(final UserPrincipal userPrincipal) {


        final Map<String, Object> claims = new HashMap<>();
        claims.put(EMAIL, userPrincipal.getEmail());
        claims.put(ROLE, userPrincipal.getRole());
        claims.put(ORGANIZATION_ID, userPrincipal.getOrganizationId());

        final Date now = new Date();

        return Jwts.builder()
                .claims(claims)
                .subject(userPrincipal.getId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + this.jwtExpiration))
                .signWith(this.getKey())
                .compact();
    }

    private SecretKey getKey() {
        final byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {

        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUserId(final String token) {

        return this.extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(final String token) {

        return this.extractClaim(token,
                claims -> claims
                        .get(EMAIL, String.class));
    }

    public String extractRole(final String token) {

        return this.extractClaim(
                token,
                claims -> claims
                        .get(ROLE, String.class));
    }

    public String extractOrganizationId(final String token) {

        return this.extractClaim(
                token,
                claims -> claims
                        .get(ORGANIZATION_ID, String.class));
    }

    private Claims extractAllClaims(final String token) {
        return Jwts.parser()
                .verifyWith(this.getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(final String token, final UserPrincipal principal) {

        return !this.isTokenExpired(token)
        && this.extractUserId(token).equals(principal.getId());
    }

    private boolean isTokenExpired(final String token) {
        return this.extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(final String token) {
        return this.extractClaim(token, Claims::getExpiration);
    }
}
