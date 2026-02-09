package dao;

import entities.Session;

public class SessionDao extends GenericDao<Session> {
    public SessionDao(jakarta.persistence.EntityManager em) {
        super(entities.Session.class, em);
    }
}
