package controllers;

import com.google.gson.JsonSyntaxException;
import dto.auth.login.LoginResponseDto;
import dto.auth.login.LoginRequestDto;
import dto.auth.refresh.AccessTokenResponseDto;
import dto.auth.signup.SignupRequestDto;
import dto.auth.signup.SignupResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.AuthService;
import utils.CookieUtil;
import utils.HttpUtil;
import utils.validate.AuthValidate;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static utils.CookieUtil.createNewCookie;

/**
 * Controller class for handling authentication-related HTTP requests.
 */
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles user signup requests.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws IOException if an input or output error is detected
     */
    public void handleSignup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Parse request body to Java object
            SignupRequestDto signupRequestDto = HttpUtil.jsonToClass(req.getReader(), SignupRequestDto.class);

            // Validate input data
            List<String> validationError = AuthValidate.validateSignup(signupRequestDto);

            // Return 400 Bad Request if validation fails
            if (!validationError.isEmpty()) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, validationError);
                return;
            }

            // Call service to signup user (after validation)
            SignupResponseDto response = authService.signup(signupRequestDto);

            // Send appropriate response based on registration outcome
            if (response.isSuccess()) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, response.getMessage());
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_CONFLICT, response.getMessage());
            }
        } catch (JsonSyntaxException e) {
            // Handle syntax errors in JSON (missing commas, brackets, etc.)
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
        }
    }

    /**
     * Handles user login requests.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws IOException if an input or output error is detected
     */
    public void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Parse request body to Java object
            LoginRequestDto loginDto = HttpUtil.jsonToClass(req.getReader(), LoginRequestDto.class);

            // Call service to authenticate user
            LoginResponseDto loginResponse = authService.login(loginDto);

            // Send appropriate response based on authentication outcome
            if (loginResponse.isSuccess()) {
                // Create refresh token cookie
                createNewCookie(resp, "refreshToken", loginResponse.getRefreshToken());

                // Remove refresh token from loginResponse body for security
                loginResponse.setRefreshToken(null);

                // Send success loginResponse with access token and user info
                HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, loginResponse);
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, loginResponse.getMessage());
            }
        } catch (JsonSyntaxException e) {
            // Handle syntax errors in JSON (missing commas, brackets, etc.)
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handles requests to refresh access tokens.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws IOException if an input or output error is detected
     */
    public void handleRefreshToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Get refresh token from cookies
            Cookie[] cookies = req.getCookies();
            if (cookies == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "No cookies found");
                return;
            }

            // Find the refreshToken cookie
            Cookie refreshToken = Stream.of(cookies).filter(c -> "refreshToken".equals(c.getName())).findFirst().orElse(
                    null);

            // Return 400 Bad Request if refresh token cookie is missing
            if (refreshToken == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Refresh token cookie not found");
                return;
            }

            // Call service to refresh access token
            AccessTokenResponseDto response = authService.refreshAccessToken(refreshToken.getValue());

            // Send appropriate response based on token refresh outcome
            if (response.isSuccess()) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, response);
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, response.getMessage());
            }
        } catch (JsonSyntaxException e) {
            // Handle syntax errors in JSON (missing commas, brackets, etc.)
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handles user logout requests.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws IOException if an input or output error is detected
     */
    public void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Get refresh token from cookies
            String refreshToken = CookieUtil.getCookieFromRequest(req, "refreshToken");

            // Delete session in database
            authService.logout(refreshToken);

            // Delete refresh token in cookie
            CookieUtil.deleteCookie(resp, "refreshToken");
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NO_CONTENT);
        } catch (JsonSyntaxException e) {
            // Handle syntax errors in JSON (missing commas, brackets, etc.)
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}
