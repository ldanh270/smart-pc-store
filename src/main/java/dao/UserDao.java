package dao;

import entities.User;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for User entity
 */
public class UserDao extends GenericDao<User> {

    public UserDao() {
        super(User.class);
    }

    /**
     * Check if a user exists by username
     *
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.username = :username";
        TypedQuery<Long> query = JPAUtil.getEntityManager().createQuery(jpql, Long.class);
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
        TypedQuery<User> query = JPAUtil.getEntityManager().createQuery(jpql, User.class);
        query.setParameter("username", username);
        return query.getSingleResult();
    }

    /**
     * Check if a user exists by email
     *
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.email = :email";
        TypedQuery<Long> query = JPAUtil.getEntityManager().createQuery(jpql, Long.class);
        query.setParameter("email", email);
        return query.getSingleResult() > 0;
    }

    /**
     * Check if username exists for another user (exclude current id).
     *
     * @param username username to check
     * @param excludeId current user id to exclude
     * @return true if another user already uses this username
     */
    public boolean existsByUsernameExceptId(String username, UUID excludeId) {
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.username = :username AND u.id <> :excludeId";
        TypedQuery<Long> query = getEntityManager().createQuery(jpql, Long.class);
        query.setParameter("username", username);
        query.setParameter("excludeId", excludeId);
        return query.getSingleResult() > 0;
    }

    /**
     * Check if email exists for another user (exclude current id).
     *
     * @param email email to check
     * @param excludeId current user id to exclude
     * @return true if another user already uses this email
     */
    public boolean existsByEmailExceptId(String email, UUID excludeId) {
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.email = :email AND u.id <> :excludeId";
        TypedQuery<Long> query = getEntityManager().createQuery(jpql, Long.class);
        query.setParameter("email", email);
        query.setParameter("excludeId", excludeId);
        return query.getSingleResult() > 0;
    }

    /**
     * Search users by keyword and paginate results.
     *
     * @param q    Keyword for username/displayName/email/id.
     * @param page Zero-based page index.
     * @param size Page size.
     * @return Matching users ordered by createdAt desc.
     */
    public List<User> searchAndPaginate(String q, Integer page, Integer size) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM User u");

        if (q != null && !q.isBlank()) {
            jpql.append(" WHERE LOWER(u.username) LIKE :q");
            jpql.append(" OR LOWER(u.displayName) LIKE :q");
            jpql.append(" OR LOWER(u.email) LIKE :q");
            jpql.append(" OR CAST(u.id AS string) LIKE :q");
        }

        jpql.append(" ORDER BY u.createdAt DESC");
        TypedQuery<User> query = getEntityManager().createQuery(jpql.toString(), User.class);

        if (q != null && !q.isBlank()) {
            query.setParameter("q", "%" + q.toLowerCase() + "%");
        }

        if (page != null && size != null) {
            query.setFirstResult(page * size);
            query.setMaxResults(size);
        }

        return query.getResultList();
    }

}
