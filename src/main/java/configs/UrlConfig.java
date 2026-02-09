package configs;

import utils.EnvHelper;

public class UrlConfig {
    public static final String CLIENT_URL = EnvHelper.get("CLIENT_URL", "http://localhost:3000");
    public static final int PORT = Integer.parseInt(EnvHelper.get("PORT", "5000"));
}
