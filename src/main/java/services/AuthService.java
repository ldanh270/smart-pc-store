package services;

import configs.JwtConfig;
import dao.SessionDao;
import dao.UserDao;
import dto.auth.AuthResponse;
import dto.auth.LoginDto;
import dto.auth.RegisterDto;
import dto.user.UserDto;
import entities.Session;
import entities.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.JwtUtil;

import java.time.Instant;
import java.util.UUID;

public class AuthService {

    private final UserDao userDao;
    private final SessionDao sessionDao;

    public AuthService(UserDao userDao, SessionDao sessionDao) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
    }

    /**
     * Registers a new user.
     *
     * @param dto The registration data transfer object containing username and password.
     * @return true if registration is successful, false if username already exists.
     */
    public boolean register(RegisterDto dto) {
        // Check if username already exists
        if (userDao.existsByUsername(dto.getUsername())) {
            return false;
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
            return true;
        } catch (Exception e) {
            // Rollback if error
            if (userDao.getEntityManager().getTransaction().isActive()) {
                userDao.getEntityManager().getTransaction().rollback();
            }

            // Log the error
            System.err.println("AuthService - register ERROR: " + e.getMessage());

            // Return failure
            return false;
        }
    }

    /**
     * Logs in a user.
     *
     * @param dto The login data transfer object containing username and password.
     * @return An AuthResponse containing user details upon successful login.
     */
    public AuthResponse login(LoginDto dto) {
        // Check if user exists
        User user = userDao.findByUsername(dto.getUsername());

        // Check if correct password
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPasswordHash())) {
            return new AuthResponse("Invalid username or password");
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
            return new AuthResponse(
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
            return new AuthResponse("Internal server error");
        }
    }
}
