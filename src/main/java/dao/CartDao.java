package dao;

import entities.Cart;
import entities.User;
import jakarta.persistence.EntityManager;

public class CartDao extends GenericDao<Cart> {
    public CartDao(EntityManager em) {
        super(Cart.class, em);
    }

    public Cart findByUser(User user) {
        try {
            return em.createQuery("SELECT c FROM Cart c WHERE c.user = :user", Cart.class)
                    .setParameter("user", user)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }
}
