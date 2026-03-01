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

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).
 */
public class JwtUtil {
    private static final int BYTE_SIZE = 64;

    /**
     * Generate JWT access token
     *
     * @param userId the user id
     * @return the JWT access token
     */
    public static String generateAccessToken(int userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", "ACCESS_TOKEN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConfig.ACCESS_TOKEN_TTL))
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Generate secure random refresh token
     *
     * @return the refresh token
     */
    public static String generateRefreshToken() {
        byte[] bytes = new byte[BYTE_SIZE];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Validates the provided JWT access token.
     *
     * @param token the JWT access token string
     * @return true if the token is valid, false if invalid or expired.
     */
    public static boolean validateAccessToken(String token) {
        try {
            // Attempt to parse the token. If successful, it is valid.
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);

            return true;

        } catch (JwtException | IllegalArgumentException ex) {
            // Catches ANY error related to JWT (expired, malformed, invalid signature)
            // or if the token string is null/empty.
            return false;
        }
    }

    /**
     * Extracts the userId from the JWT access token.
     *
     * @param token the JWT access token string
     * @return the userId (Integer) if valid, or null if an error occurs
     */
    public static Integer getUserIdFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("userId", Integer.class);
        } catch (JwtException | IllegalArgumentException ex) {
            // Returns null if the token is invalid, expired, or tampered with
            return null;
        }
    }

    /**
     * Extracts the role from the JWT access token
     *
     * @param token the JWT access token string
     * @return the role (String) if valid, or null if an error occurs
     */
    public static String getRoleFromToken(String token) {
        try {
            return extractAllClaims(token).get("role", String.class);
        } catch (JwtException ex) {
            return null;
        }
    }

    /*
     * ===== PRIVATE HELPER METHODS =====
     */

    /**
     * Helper method to decode and extract all claims from the JWT token
     * * @param token the JWT access token string
     *
     * @return the Claims object containing all token payload data
     */
    private static Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Helper method to get the signing key for JWT generation and validation
     *
     * @return the Key object used for signing and verifying JWTs
     */
    private static Key getSignInKey() {
        byte[] keyBytes = JwtConfig.ACCESS_TOKEN_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Parse JWT từ Authorization header và trả về userId.
     * Header format: "Bearer <token>"
     */
    public static Integer getUserIdFromAuthorizationHeader(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = header.substring(7); // Bỏ "Bearer " (7 ký tự)
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
}
