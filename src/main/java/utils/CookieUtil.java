package utils;

import configs.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility class for handling cookies.
 */
public class CookieUtil {
    /**
     * Retrieves the value of a specific cookie from the request.
     *
     * @param req        the HttpServletRequest object
     * @param cookieName the name of the cookie to retrieve
     * @return the value of the cookie, or null if not found
     */
    public static String getCookieFromRequest(HttpServletRequest req, String cookieName) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Creates a new HttpOnly, Secure cookie and adds it to the response.
     *
     * @param resp        the HttpServletResponse object
     * @param cookieName  the name of the cookie
     * @param cookieValue the value of the cookie
     */
    public static void createNewCookie(HttpServletResponse resp, String cookieName, String cookieValue) {
        // Create HttpOnly, Secure cookie for refresh token
        Cookie cookie = new Cookie(cookieName, cookieValue);

        // Set cookie attributes
        cookie.setHttpOnly(true);

        // Set Secure attribute to true to ensure cookie is sent over HTTPS only
        cookie.setSecure(true);

        // Set the path for which the cookie is valid
        cookie.setPath("/");

        // Set the maximum age of the cookie
        cookie.setMaxAge(JwtConfig.REFRESH_TOKEN_TTL);

        // Add cookie to response
        resp.addCookie(cookie);
    }

    /**
     * Deletes a cookie by setting its max age to zero (overwrite).
     *
     * @param resp the HttpServletResponse object
     * @param name the name of the cookie to delete
     */
    public static void deleteCookie(HttpServletResponse resp, String cookieName) {
        // Create a cookie with the same cookieName and set its max age to zero
        Cookie cookie = new Cookie(cookieName, null);

        // Set cookie attributes to ensure it is deleted properly
        cookie.setPath("/");

        // Set HttpOnly to true for security
        cookie.setHttpOnly(true);

        // Set Secure to true to match the original cookie's attributes
        cookie.setMaxAge(0);

        // Add cookie to response to overwrite the existing one
        resp.addCookie(cookie);
    }
}

