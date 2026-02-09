package utils;

import configs.JwtConfig;
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

    // Helper method to get signing key
    private static Key getSignInKey() {
        byte[] keyBytes = JwtConfig.ACCESS_TOKEN_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
