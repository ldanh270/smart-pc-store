package dao;

import entities.Session;

/**
 * Data Access Object for Session entity.
 */
public class SessionDao extends GenericDao<Session> {
    public SessionDao(jakarta.persistence.EntityManager em) {
        super(entities.Session.class, em);
    }

    /**
     * Find a session by its refresh token.
     *
     * @param refreshToken the refresh token
     * @return the Session entity if found, otherwise null
     */
    public Session findByRefreshToken(String refreshToken) {
        try {
            return em.createQuery("SELECT s FROM Session s WHERE s.refreshToken = :refreshToken", Session.class)
                    .setParameter("refreshToken", refreshToken)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }
}
