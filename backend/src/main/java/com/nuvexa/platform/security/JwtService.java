package com.nuvexa.platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-minutes:15}")
    private long expirationMinutes;

    public long getExpiracaoSegundos() {
        return expirationMinutes * 60;
    }

    /** O claim {@code sid} liga o access token à sessão (refresh token) que o originou. */
    public String generateToken(UserDetails userDetails, Long sessaoId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMinutes * 60_000);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("sid", sessaoId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractSessaoId(String token) {
        Number sid = extractClaim(token, claims -> claims.get("sid", Number.class));
        return sid == null ? null : sid.longValue();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && userDetails.isEnabled() && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
