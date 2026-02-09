package dao;

import entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class UserDao extends GenericDao<User> {
    /**
     * Constructor
     *
     * @param em the entity manager
     */
    public UserDao(EntityManager em) {
        super(User.class, em);
    }

    /**
     * Check if a user exists by username
     *
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.username = :username";
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("username", username);
        return query.getSingleResult() > 0;
    }

    /**
     * Find a user by username
     *
     * @param username the username to search for
     * @return the User entity
     */
    public User findByUsername(String username) {
        String jpql = "SELECT u FROM User u WHERE u.username = :username";
        TypedQuery<User> query = em.createQuery(jpql, User.class);
        query.setParameter("username", username);
        return query.getSingleResult();
    }
}
