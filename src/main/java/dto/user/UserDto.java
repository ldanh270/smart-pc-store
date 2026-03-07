package dto.user;

import java.util.UUID;

/**
 * Data Transfer Object for User information.
 */
public class UserDto {
    private UUID id;
    private String username;
    private String displayName;
    private String email;
    private String phone;
    private String address;
    private String status;
    private String role;

    /**
     * Constructor to initialize all fields of UserDto.
     *
     * @param id          the user ID
     * @param username    the username
     * @param displayName the full name of the user
     * @param email       the email address
     * @param phone       the phone number
     * @param address     the physical address
     * @param status      the account status
     */
    public UserDto(
            UUID id,
            String username,
            String displayName,
            String email,
            String phone,
            String address,
            String status
    ) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
    }

    public UserDto(
            UUID id,
            String username,
            String displayName,
            String email,
            String phone,
            String address,
            String status,
            String role
    ) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
