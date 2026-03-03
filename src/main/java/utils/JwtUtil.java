package utils;

import configs.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).
 */
public class JwtUtil {
    private static final int BYTE_SIZE = 64;

    /**
     * Generate JWT access token with default USER role.
     */
    public static String generateAccessToken(int userId) {
        return generateAccessToken(userId, null, "USER");
    }

    /**
     * Generate JWT access token with explicit username and role claims.
     */
    public static String generateAccessToken(int userId, String username, String role) {
        String normalizedRole = role == null || role.isBlank() ? "USER" : role.toUpperCase(Locale.ROOT);
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", normalizedRole)
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConfig.ACCESS_TOKEN_TTL))
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Generate secure random refresh token.
     */
    public static String generateRefreshToken() {
        byte[] bytes = new byte[BYTE_SIZE];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Validate the provided JWT access token.
     */
    public static boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Extract userId from access token.
     */
    public static Integer getUserIdFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("userId", Integer.class);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Extract role from access token.
     */
    public static String getRoleFromToken(String token) {
        try {
            return extractAllClaims(token).get("role", String.class);
        } catch (JwtException ex) {
            return null;
        }
    }

    /**
     * Parse JWT from Authorization header and return userId.
     * Header format: "Bearer <token>"
     */
    public static Integer getUserIdFromAuthorizationHeader(String header) {
        String token = extractBearerToken(header);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return ((Number) claims.get("userId")).intValue();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired token: " + e.getMessage());
        }
    }

    /**
     * Parse JWT from Authorization header and return role claim.
     */
    public static String getRoleFromAuthorizationHeader(String header) {
        String token = extractBearerToken(header);
        String role = getRoleFromToken(token);
        if (role == null || role.isBlank()) {
            throw new RuntimeException("Missing role claim in token");
        }
        return role;
    }

    private static String extractBearerToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return header.substring(7);
    }

    private static Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    private static Key getSignInKey() {
        byte[] keyBytes = JwtConfig.ACCESS_TOKEN_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
