package dto.auth.login;

/**
 * Data Transfer Object for user login credentials.
 */
public class LoginRequestDto {
    private String username;
    private String password;

    public LoginRequestDto() {
    }

    /**
     * Constructs a LoginRequestDto with the specified username and password.
     *
     * @param username the username of the user
     * @param password the password of the user
     */
    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
