package controllers;

import com.google.gson.JsonSyntaxException;
import dto.auth.AuthResponse;
import dto.auth.LoginDto;
import dto.auth.RegisterDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.AuthService;
import utils.HttpUtil;
import utils.validate.AuthValidate;

import java.io.IOException;
import java.util.List;

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
            RegisterDto registerDto = HttpUtil.jsonToClass(req.getReader(), RegisterDto.class);

            // Validate input data
            List<String> validationError = AuthValidate.validateRegister(registerDto);

            // Return 400 Bad Request if validation fails
            if (!validationError.isEmpty()) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, validationError.toString());
                return;
            }

            // Call service to register user (after validation)
            boolean success = authService.register(registerDto);

            // Send appropriate response based on registration outcome
            if (success) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, "Register successfully");
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_CONFLICT, "User already exists");
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
            LoginDto loginDto = HttpUtil.jsonToClass(req.getReader(), LoginDto.class);

            // Call service to authenticate user
            AuthResponse response = authService.login(loginDto);

            // Send appropriate response based on authentication outcome
            if (response.isSuccess()) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, response);
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, response.getMessage());
            }
        } catch (JsonSyntaxException e) {
            // Handle syntax errors in JSON (missing commas, brackets, etc.)
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
        }
    }
}
