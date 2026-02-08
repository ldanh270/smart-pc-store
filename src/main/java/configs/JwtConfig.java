package configs;

import utils.EnvHelper;

/**
 * JwtConfig class to manage JWT token settings.
 */
public class JwtConfig {
    public static final String ACCESS_TOKEN_SECRET = EnvHelper.get("ACCESS_TOKEN_SECRET", "");
    public static final long ACCESS_TOKEN_TTL = 15 * 60 * 1000; // 15 minutes
    public static final long REFRESH_TOKEN_TTL = 7 * 24 * 60 * 60 * 1000; // 7 days
}
