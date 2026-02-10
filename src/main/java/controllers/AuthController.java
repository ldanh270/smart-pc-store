package controllers;

import com.google.gson.JsonSyntaxException;
import dto.auth.login.LoginResponseDto;
import dto.auth.login.LoginRequestDto;
import dto.auth.signup.SignupRequestDto;
import dto.auth.signup.SignupResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.AuthService;
import utils.HttpUtil;
import utils.validate.AuthValidate;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
            LoginResponseDto response = authService.login(loginDto);

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
