package utils.validate;

import configs.Regex;
import dto.auth.RegisterDto;

import java.util.ArrayList;
import java.util.List;

public class AuthValidate {

    /**
     * Validates the registration data.
     *
     * @param dto The registration data transfer object.
     * @return A list of validation error messages. Empty if no errors.
     */
    public static List<String> validateRegister(RegisterDto dto) {
        // List to hold validation error messages
        List<String> errors = new ArrayList<>();

        // Check for null dto
        if (dto == null) {
            errors.add("Request body must not empty");
            return errors;
        }

        // Validate username
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            errors.add("Username is required");
        }

        // Validate password
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            errors.add("Password must be at least 6 characters");
        }

        // Validate full name
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            errors.add("Fullname is required");
        }

        // Validate email
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            errors.add("Email is required");
        }

        // Email format check
        if (!dto.getEmail().matches(Regex.EMAIL_REGEX)) {
            errors.add("Invalid email format");
        }

        // Return list of errors
        return errors;
    }
}