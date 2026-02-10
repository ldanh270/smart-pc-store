package dto.auth.refresh;

/**
 * Data Transfer Object for Access Token Response.
 */
public class AccessTokenResponseDto {
    private boolean success;
    private String accessToken;
    private String message;

    public AccessTokenResponseDto(boolean success, String accessToken, String message) {
        this.success = success;
        this.accessToken = accessToken;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
