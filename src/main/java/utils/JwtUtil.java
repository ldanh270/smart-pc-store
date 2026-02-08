package utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).
 */
public class JwtUtil {
    private static final String SECRET_KEY_STRING = "day_la_khoa_bi_mat_rat_dai_va_bao_mat_cua_ban_123456_phai_du_32_byte";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    // Thời gian hết hạn:
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 30; // 30 phút
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 ngày

    // ================= 1. GENERATE TOKEN =================

    // Tạo Access Token (Ngắn hạn, chứa role)
    public static String generateAccessToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        // Có thể thêm userId, email vào claims nếu cần
        return createToken(claims, username, ACCESS_TOKEN_EXPIRATION);
    }

    // Tạo Refresh Token (Dài hạn, thường ít thông tin hơn)
    public static String generateRefreshToken(String username) {
        return createToken(new HashMap<>(), username, REFRESH_TOKEN_EXPIRATION);
    }

    // Hàm chung để tạo token
    private static String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // ================= 2. EXTRACT DATA (Lấy thông tin) =================

    // Lấy Username (Subject) từ Token
    public static String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Lấy Role từ Token
    public static String getRoleFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }

    // Lấy ngày hết hạn
    public static Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Helper: Trích xuất 1 claim cụ thể
    public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Helper: Parse toàn bộ Body của Token
    private static Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ================= 3. VALIDATION (Kiểm tra) =================

    // Validate Refresh Token and return username if valid
    public static boolean validateRefreshToken(String token, String userDetailsUsername) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetailsUsername) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Chỉ kiểm tra xem token còn sống hay không (Dùng cho filter)
    public static boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            // Có thể log lỗi ở đây: System.out.println(e.getMessage());
            return false;
        }
    }

    private static boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }
}