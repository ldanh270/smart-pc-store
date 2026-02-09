package dto.auth;

public class LoginDto {
    private String username;
    private String password;

    // BẮT BUỘC: Constructor rỗng để Gson/Jackson khởi tạo
    public LoginDto() {
    }

    public LoginDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter & Setter (Bắt buộc để thư viện đọc/ghi giá trị)
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