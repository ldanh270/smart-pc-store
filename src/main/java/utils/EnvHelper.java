package utils;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Helper class for loading environment variables using Dotenv.
 */
public class EnvHelper {
    // ignoreIfMissing: To allow running without a .env file (for production environments)
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    // Get environment variable by key (with fail-fast for missing important vars)
    public static String get(String key) {
        String value = dotenv.get(key);

        // Fail-fast: Log warning if a critical env var is missing
        if (value == null) {
            System.err.println("WARNING: Missing environment variable " + key);
        }
        return value;
    }

    // Get environment variable by key with default value
    public static String get(String key, String defaultValue) {
        return dotenv.get(key, defaultValue);
    }
}
