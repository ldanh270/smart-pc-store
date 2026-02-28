package utils;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Helper class for loading environment variables using Dotenv.
 */
public class EnvHelper {
    // Tìm .env theo thứ tự ưu tiên:
    // 1. Working directory (thường là thư mục Tomcat bin/)
    // 2. Classpath root (src/main/resources/.env → đóng gói vào WAR)
    private static final Dotenv dotenv = buildDotenv();

    private static Dotenv buildDotenv() {
        // Thử load từ working directory trước
        try {
            return Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            // Fallback: load từ classpath
            return Dotenv.configure()
                    .directory(EnvHelper.class.getClassLoader().getResource("").getPath())
                    .ignoreIfMissing()
                    .load();
        }
    }

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
