package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "Username")
    private String username;

    @Nationalized
    @Column(name = "PasswordHash")
    private String passwordHash;

    @Nationalized
    @Column(name = "FullName")
    private String fullName;

    @Nationalized
    @Column(name = "Email")
    private String email;

    @Nationalized
    @Column(name = "Phone")
    private String phone;

    @Nationalized
    @Column(name = "Address")
    private String address;

    @Nationalized
    @Column(name = "Status")
    private String status;

    @Column(name = "CreatedAt")
    private Instant createdAt;

    public User(String username, String passwordHash, String fullName, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = Instant.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}