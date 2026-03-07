package services;

import dao.UserDao;
import dto.user.CreateUserRequestDto;
import dto.user.UpdateUserRequestDto;
import dto.user.UserDto;
import entities.User;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.UUID;

/**
 * UserService
 * <p>
 * Responsibilities:
 * - CRUD operations for User entity
 * - Validate uniqueness (username/email)
 * - Hash password when creating/updating
 */
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getEmail(),
                u.getPhone(),
                u.getAddress(),
                u.getStatus(),
                u.getRole()
        );
    }

    public List<UserDto> getAll() {
        return userDao.findAll().stream().map(this::toDto).toList();
    }

    public UserDto getById(UUID id) {
        User u = userDao.findById(id);
        if (u == null) throw new RuntimeException("User not found");
        return toDto(u);
    }

    public UserDto create(CreateUserRequestDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank())
            throw new RuntimeException("Username is required");
        if (dto.getPassword() == null || dto.getPassword().isBlank())
            throw new RuntimeException("Password is required");
        if (dto.getEmail() == null || dto.getEmail().isBlank()) throw new RuntimeException("Email is required");

        if (userDao.existsByUsername(dto.getUsername())) throw new RuntimeException("Username is already exists");
        if (userDao.existsByEmail(dto.getEmail())) throw new RuntimeException("Email is already exists");

        try {
            userDao.getEntityManager().getTransaction().begin();

            String passwordHash = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());

            User u = new User();
            u.setUsername(dto.getUsername());
            u.setPasswordHash(passwordHash);
            u.setDisplayName(dto.getDisplayName());
            u.setEmail(dto.getEmail());
            u.setPhone(dto.getPhone());
            u.setAddress(dto.getAddress());
            u.setStatus(dto.getStatus());

            userDao.create(u);

            userDao.getEntityManager().getTransaction().commit();
            return toDto(u);

        } catch (Exception e) {
            if (userDao.getEntityManager().getTransaction().isActive())
                userDao.getEntityManager().getTransaction().rollback();
            throw e;
        }
    }

    public UserDto update(UUID id, UpdateUserRequestDto dto) {
        User u = userDao.findById(id);
        if (u == null) throw new RuntimeException("User not found");

        // Uniqueness checks only if user changes these fields
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            if (userDao.existsByUsernameExceptId(dto.getUsername(), id)) throw new RuntimeException(
                    "Username is already exists");
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (userDao.existsByEmailExceptId(dto.getEmail(), id))
                throw new RuntimeException("Email is already exists");
        }

        try {
            userDao.getEntityManager().getTransaction().begin();

            if (dto.getUsername() != null && !dto.getUsername().isBlank()) u.setUsername(dto.getUsername());

            if (dto.getDisplayName() != null) u.setDisplayName(dto.getDisplayName());

            if (dto.getEmail() != null && !dto.getEmail().isBlank()) u.setEmail(dto.getEmail());

            if (dto.getPhone() != null) u.setPhone(dto.getPhone());

            if (dto.getAddress() != null) u.setAddress(dto.getAddress());

            if (dto.getStatus() != null) u.setStatus(dto.getStatus());

            // Optional password update
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                String passwordHash = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());
                u.setPasswordHash(passwordHash);
            }

            if (dto.getRole() != null) u.setRole(dto.getRole());

            userDao.update(u);

            userDao.getEntityManager().getTransaction().commit();
            return toDto(u);

        } catch (Exception e) {
            if (userDao.getEntityManager().getTransaction().isActive())
                userDao.getEntityManager().getTransaction().rollback();
            throw e;
        }
    }

    public void delete(UUID id) {
        User u = userDao.findById(id);
        if (u == null) throw new RuntimeException("User not found");

        try {
            EntityManager em = userDao.getEntityManager();
            em.getTransaction().begin();

            // Delete Payments associated with the user's Orders
            em.createQuery("DELETE FROM Payment p WHERE p.order.id IN " + "(SELECT o.id FROM Order o WHERE o.user.id = :userId)")
                    .setParameter("userId", id)
                    .executeUpdate();
            
            // Delete Orders of user
            em.createQuery("DELETE FROM Order o WHERE o.user.id = :userId").setParameter("userId", id).executeUpdate();

            //  Delete CartItems associated with the user's Carts
            em.createQuery("DELETE FROM CartItem ci WHERE ci.cart.id IN " + "(SELECT c.id FROM Cart c WHERE c.user.id = :userId)")
                    .setParameter("userId", id)
                    .executeUpdate();

            //  Delete Carts of user
            em.createQuery("DELETE FROM Cart c WHERE c.user.id = :userId").setParameter("userId", id).executeUpdate();

            //  Delete Sessions of user
            em.createQuery("DELETE FROM Session s WHERE s.user.id = :userId")
                    .setParameter("userId", id)
                    .executeUpdate();

            // 7. Delete User
            userDao.delete(id);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (userDao.getEntityManager().getTransaction().isActive())
                userDao.getEntityManager().getTransaction().rollback();
            throw e;
        }
    }
}
