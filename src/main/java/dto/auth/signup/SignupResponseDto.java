package dto.auth.signup;

/**
 * Data Transfer Object for Signup Response
 */
public class SignupResponseDto {
    private boolean success;
    private String message;

    public SignupResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
