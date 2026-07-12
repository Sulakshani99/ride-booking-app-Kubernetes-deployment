package com.ridebooking.ride.security;

import com.ridebooking.ride.exception.RideServiceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtClaimsExtractor {

    private final SecretKey signInKey;

    public JwtClaimsExtractor(@Value("${jwt.secret}") String secret) 
    {
        this.signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public Long extractUserId(String authorizationHeader) 
    {
        String token = extractBearerToken(authorizationHeader);
        Claims claims = Jwts.parser()
                .verifyWith(signInKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String value && !value.isBlank()) {
            return Long.parseLong(value);
        }
        throw new RideServiceException("JWT does not contain a userId claim");
    }

    public String extractRole(String authorizationHeader)
    {
        String token = extractBearerToken(authorizationHeader);
        Claims claims = Jwts.parser()
                .verifyWith(signInKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String role = claims.get("role", String.class);
        if (role != null && !role.isBlank()) {
            return role;
        }
        throw new RideServiceException("JWT does not contain a role claim");
    }

    public String extractEmail(String authorizationHeader)
    {
        String token = extractBearerToken(authorizationHeader);
        Claims claims = Jwts.parser()
                .verifyWith(signInKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String email = claims.getSubject();
        if (email != null && !email.isBlank()) {
            return email;
        }
        throw new RideServiceException("JWT does not contain an email subject");
    }

    private String extractBearerToken(String authorizationHeader) 
    {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) 
        {
            throw new RideServiceException("Missing or invalid Authorization header");
        }
        return authorizationHeader.substring("Bearer ".length());
    }
    
}
