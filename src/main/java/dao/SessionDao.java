package dao;

import java.util.UUID;

import entities.Session;

/**
 * Data Access Object for Session entity.
 */
public class SessionDao extends GenericDao<Session> {
    public SessionDao() {
        super(entities.Session.class);
    }

    /**
     * Find a session by its refresh token.
     *
     * @param refreshToken the refresh token
     * @return the Session entity if found, otherwise null
     */
    public Session findByRefreshToken(String refreshToken) {
        try {
            return getEntityManager().createQuery(
                    "SELECT s FROM Session s WHERE s.refreshToken = :refreshToken",
                    Session.class
            ).setParameter("refreshToken", refreshToken).getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }

    /**
     * Delete all sessions for a specific user.
     *
     * @param userId the user ID whose sessions should be deleted
     */
    public void deleteByUserId(UUID userId) {
        getEntityManager().createQuery("DELETE FROM Session s WHERE s.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    /**
     * Delete all expired sessions.
     *
     * @return the number of deleted sessions
     */
    public int deleteExpiredSessions() {
        jakarta.persistence.EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            int deletedCount = em.createQuery("DELETE FROM Session s WHERE s.expiredAt < :now")
                    .setParameter("now", java.time.OffsetDateTime.now())
                    .executeUpdate();
            em.getTransaction().commit();
            return deletedCount;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
}
