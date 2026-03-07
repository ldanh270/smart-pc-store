package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"Users\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "username", length = Integer.MAX_VALUE)
    private String username;

    @Column(name = "\"passwordHash\"", length = Integer.MAX_VALUE)
    private String passwordHash;

    @Column(name = "\"displayName\"", length = Integer.MAX_VALUE)
    private String displayName;

    @Column(name = "email", length = Integer.MAX_VALUE)
    private String email;

    @Column(name = "phone", length = Integer.MAX_VALUE)
    private String phone;

    @Column(name = "address", length = Integer.MAX_VALUE)
    private String address;

    @Column(name = "status", length = Integer.MAX_VALUE)
    private String status;

    @Column(name = "role", length = Integer.MAX_VALUE)
    private String role;

    @ColumnDefault("now()")
    @Column(name = "\"createdAt\"")
    private OffsetDateTime createdAt;


    public User(String username, String passwordHash, String displayName, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.email = email;
    }

    public User(
            String username,
            String passwordHash,
            String displayName,
            String email,
            String phone,
            String address,
            String status
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
    }

    public User() {

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
