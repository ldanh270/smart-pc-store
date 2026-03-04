package utils;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

/**
 * Helper class for loading environment variables using Dotenv
 */
public class EnvHelper {

    private static final Dotenv dotenv = buildDotenv();

    private static Dotenv buildDotenv() {
        try {
            return Dotenv.configure().ignoreIfMissing().load();
        } catch (DotenvException e) {
            return Dotenv.configure()
                    .directory(EnvHelper.class.getClassLoader().getResource("").getPath())
                    .ignoreIfMissing()
                    .load();
        }
    }

    public static String get(String key) {
        String value = dotenv.get(key);

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
