package configs;

import utils.EnvHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

/**
 * JwtConfig class to manage JWT token settings.
 */
public class JwtConfig {

    // Secret key for signing access tokens
    public static final String ACCESS_TOKEN_SECRET;

    // Token time-to-live settings in milliseconds
    public static final int ACCESS_TOKEN_TTL = 15 * 60 * 1000; // 15 minutes

    // Refresh token time-to-live settings in milliseconds
    public static final int REFRESH_TOKEN_TTL = 7 * 24 * 60 * 60 * 1000; // 7 days

    // Static block to initialize the ACCESS_TOKEN_SECRET from environment variables
    static {
        String secret = EnvHelper.get("ACCESS_TOKEN_SECRET");
        if (secret == null || secret.isEmpty()) {
            throw new RuntimeException("CRITICAL ERROR: ACCESS_TOKEN_SECRET is missing in .env file!");
        }
        ACCESS_TOKEN_SECRET = secret;
    }

    public static Integer getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
             .setSigningKey(getSignInKey())
             .build()
             .parseClaimsJws(token)
             .getBody();
            return claims.get("userId", Integer.class);
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired token");
        }
    }

    public static Integer getUserIdFromAuthorizationHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return getUserIdFromToken(token);
    }

    private static byte[] getSignInKey() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
