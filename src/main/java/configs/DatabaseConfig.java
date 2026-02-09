package configs;

import utils.EnvHelper;

/**
 * Database configuration class to manage database connection settings.
 */
public class DatabaseConfig {
    public static final String URL = EnvHelper.get("DB_URL", "jdbc:mysql://localhost:3306/ecommerce_db");
    public static final String USERNAME = EnvHelper.get("DB_USERNAME", "root");
    public static final String PASSWORD = EnvHelper.get("DB_PASSWORD", "password");
    public static final String DB_DRIVER = EnvHelper.get("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
}
