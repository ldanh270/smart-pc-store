package services;

import configs.JwtConfig;
import dao.SessionDao;
import dao.UserDao;
import dto.auth.login.LoginResponseDto;
import dto.auth.login.LoginRequestDto;
import dto.auth.refresh.AccessTokenResponseDto;
import dto.auth.signup.SignupRequestDto;
import dto.auth.signup.SignupResponseDto;
import dto.user.UserDto;
import entities.Session;
import entities.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.JwtUtil;

import java.time.Instant;
import java.util.UUID;

/**
 * Service class for handling authentication-related operations.
 */
public class AuthService {

    private final UserDao userDao;
    private final SessionDao sessionDao;

    public AuthService(UserDao userDao, SessionDao sessionDao) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
    }

    /**
     * Signup a new user.
     *
     * @param dto The signup data transfer object containing user details.
     * @return A SignupResponseDto indicating the success or failure of the registration.
     */
    public SignupResponseDto signup(SignupRequestDto dto) {
        // Check if username already exists
        if (userDao.existsByUsername(dto.getUsername())) {
            return new SignupResponseDto(false, "Username is already exists");
        }

        // Check if email already exists
        if (userDao.existsByEmail(dto.getEmail())) {
            return new SignupResponseDto(false, "Email is already exists");
        }

        try {
            // Begin transaction
            userDao.getEntityManager().getTransaction().begin();

            // Hash the password
            String passwordHash = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());

            // Create new user
            userDao.create(new User(dto.getUsername(), passwordHash, dto.getFullName(), dto.getEmail()));

            // Commit transaction
            userDao.getEntityManager().getTransaction().commit();

            // Return success
            return new SignupResponseDto(true, "Register successfully");
        } catch (Exception e) {
            // Rollback if error
            if (userDao.getEntityManager().getTransaction().isActive()) {
                userDao.getEntityManager().getTransaction().rollback();
            }

            // Log the error
            System.err.println("AuthService - register ERROR: " + e.getMessage());

            // Return failure
            return new SignupResponseDto(false, "Internal server error");
        }
    }

    /**
     * Login a user.
     *
     * @param dto The login data transfer object containing username and password.
     * @return An LoginResponseDto containing user details upon successful login.
     */
    public LoginResponseDto login(LoginRequestDto dto) {
        // Check if user exists
        User user = userDao.findByUsername(dto.getUsername());

        // Check if correct password
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPasswordHash())) {
            return new LoginResponseDto("Invalid username or password");
        }

        try {
            // Create access token (JWT)
            String accessToken = JwtUtil.generateAccessToken(user.getId());

            // // Create refresh token (UUID)
            String refreshToken = UUID.randomUUID().toString();

            // Create session in DB
            sessionDao.getEntityManager().getTransaction().begin();

            Session session = new Session();

            session.setUser(user);

            session.setRefreshToken(refreshToken);

            // Set expiration date for refresh token (e.g., 7 days)
            session.setExpiredAt(Instant.now().plusMillis(JwtConfig.REFRESH_TOKEN_TTL));

            sessionDao.create(session);
            sessionDao.getEntityManager().getTransaction().commit();

            // Respond with access token, refresh token and user info
            return new LoginResponseDto(
                    accessToken, refreshToken, new UserDto(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getStatus()
            )
            );

        } catch (Exception e) {
            if (sessionDao.getEntityManager().getTransaction().isActive()) {
                sessionDao.getEntityManager().getTransaction().rollback();
            }
            System.err.println("AuthService - login ERROR: " + e.getMessage());
            return new LoginResponseDto("Internal server error");
        }
    }

    /**
     * Refresh access token using the provided refresh token.
     *
     * @param refreshToken The refresh token.
     * @return An AccessTokenResponseDto containing the new access token.
     */
    public AccessTokenResponseDto refreshAccessToken(String refreshToken) {
        // Check if session exists with the provided refresh token
        Session session = sessionDao.findByRefreshToken(refreshToken);

        // Validate session and expiration
        if (session == null || session.getExpiredAt().isBefore(Instant.now())) {
            return new AccessTokenResponseDto(false, null, "Invalid or expired refresh token");
        }

        try {
            // Generate new access token
            String newAccessToken = JwtUtil.generateAccessToken(session.getUser().getId());

            // Return new access token
            return new AccessTokenResponseDto(true, newAccessToken, "Access token refreshed successfully");
        } catch (Exception e) {
            if (sessionDao.getEntityManager().getTransaction().isActive()) {
                sessionDao.getEntityManager().getTransaction().rollback();
            }
            System.err.println("AuthService - refreshToken ERROR: " + e.getMessage());
            return new AccessTokenResponseDto(false, null, "Internal server error");
        }
    }
}
