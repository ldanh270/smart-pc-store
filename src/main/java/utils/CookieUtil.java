package utils;

import configs.JwtConfig;
import dto.auth.login.LoginResponseDto;
import jakarta.servlet.http.Cookie;

/**
 * Utility class for handling cookies.
 */
public class CookieUtil {
    public static Cookie getCookie(LoginResponseDto response) {
        // Create HttpOnly, Secure cookie for refresh token
        Cookie refreshTokenCookie = new Cookie("refreshToken", response.getRefreshToken());

        // Set cookie attributes
        refreshTokenCookie.setHttpOnly(true);

        // Set Secure attribute to true to ensure cookie is sent over HTTPS only
        refreshTokenCookie.setSecure(true);

        // Set the path for which the cookie is valid
        refreshTokenCookie.setPath("/");

        // Set the maximum age of the cookie
        refreshTokenCookie.setMaxAge(JwtConfig.REFRESH_TOKEN_TTL);

        // Set SameSite attribute to Strict for CSRF protection
        return refreshTokenCookie;
    }
}
