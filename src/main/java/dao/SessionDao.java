package dao;

import entities.Session;

/**
 * Data Access Object for Session entity.
 *
 */
public class SessionDao extends GenericDao<Session> {
    public SessionDao(jakarta.persistence.EntityManager em) {
        super(entities.Session.class, em);
    }
}
