package services;

import configs.JwtConfig;
import dao.SessionDao;
import dao.UserDao;
import dto.auth.login.LoginResponseDto;
import dto.auth.login.LoginRequestDto;
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
     * Register a new user.
     *
     * @param dto The registration data transfer object containing username and password.
     * @return true if registration is successful, false if username already exists.
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

            // Set expiration date (30 days from now)
            session.setExpiredAt(Instant.now().plusMillis(JwtConfig.REFRESH_TOKEN_TTL));

            sessionDao.create(session);
            sessionDao.getEntityManager().getTransaction().commit();

            // 6. Trả về kết quả thành công
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
}
