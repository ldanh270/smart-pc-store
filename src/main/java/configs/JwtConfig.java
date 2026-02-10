package configs;

import utils.EnvHelper;

/**
 * JwtConfig class to manage JWT token settings.
 */
public class JwtConfig {
    // Secret key for signing access tokens
    public static final String ACCESS_TOKEN_SECRET;

    // Token time-to-live settings in milliseconds
    public static final long ACCESS_TOKEN_TTL = 15 * 60 * 1000; // 15 minutes

    // Refresh token time-to-live settings in milliseconds
    public static final long REFRESH_TOKEN_TTL = 7 * 24 * 60 * 60 * 1000; // 7 days

    // Static block to initialize the ACCESS_TOKEN_SECRET from environment variables
    static {
        String secret = EnvHelper.get("ACCESS_TOKEN_SECRET");
        if (secret == null || secret.isEmpty()) {
            throw new RuntimeException("CRITICAL ERROR: ACCESS_TOKEN_SECRET is missing in .env file!");
        }
        ACCESS_TOKEN_SECRET = secret;
    }
}
