package com.ridebooking.auth.service;

import com.ridebooking.auth.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class JwtService {

    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";

    private final String secret;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final Map<String, Date> userLoggedOutAt = new ConcurrentHashMap<>();

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("#{${jwt.access-token-expiration-ms}}") long accessTokenExpirationMs,
            @Value("#{${jwt.refresh-token-expiration-ms}}") long refreshTokenExpirationMs
    ) {
        this.secret = secret;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", ACCESS_TOKEN_TYPE);
        enrichIdentityClaims(claims, userDetails);
        return buildToken(claims, userDetails, accessTokenExpirationMs);
    }

    //check this
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", REFRESH_TOKEN_TYPE);
        enrichIdentityClaims(claims, userDetails);
        return buildToken(claims, userDetails, refreshTokenExpirationMs);
    }

    private void enrichIdentityClaims(Map<String, Object> claims, UserDetails userDetails) {
        if (userDetails instanceof AppUser appUser) {
            claims.put("userId", appUser.getId());
            claims.put("role", appUser.getRole().name());
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails, String expectedType) {
        String username = extractUsername(token);
        String tokenType = extractClaim(token, claims -> claims.get("token_type", String.class));
        Date issuedAt = extractClaim(token, Claims::getIssuedAt);
        Date logoutAt = userLoggedOutAt.get(username);
        boolean tokenRevokedByLogout = logoutAt != null && issuedAt != null && !issuedAt.after(logoutAt);

        return username.equals(userDetails.getUsername())
                && expectedType.equals(tokenType)
                && !tokenRevokedByLogout
                && !isTokenExpired(token);
    }

    public void invalidateUserTokens(String token) {
        String username = extractUsername(token);
        userLoggedOutAt.put(username, new Date());
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expirationMs
    ) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSignInKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
