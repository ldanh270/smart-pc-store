package dto.auth.login;

import dto.user.UserDto;

/**
 * Data Transfer Object (DTO) for authentication responses.
 */
public class LoginResponseDto {
    private boolean success;
    private String accessToken;
    private String refreshToken;
    private UserDto user;
    private String message;

    public LoginResponseDto() {
    }

    /**
     * Constructor for successful authentication response.
     *
     * @param accessToken  the access token
     * @param refreshToken the refresh token
     */
    public LoginResponseDto(String accessToken, String refreshToken, UserDto user) {
        this.success = true;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    /**
     * Constructor for failed authentication response.
     *
     * @param message the failure message
     */
    public LoginResponseDto(String message) {
        this.success = false;
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

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}
