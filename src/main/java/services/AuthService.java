package services;

import dao.UserDao;
import dto.auth.RegisterDto;
import entities.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Registers a new user.
     *
     * @param dto The registration data transfer object containing username and password.
     * @return - true if registration is successful
     * - false if username already exists.
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
}