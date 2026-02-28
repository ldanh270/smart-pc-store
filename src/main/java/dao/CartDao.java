package dao;

import entities.Cart;
import entities.User;
import jakarta.persistence.EntityManager;

public class CartDao extends GenericDao<Cart> {
    public CartDao() {
        super(Cart.class);
    }

    public Cart findByUser(User user) {
        try {
            return getEntityManager().createQuery("SELECT c FROM Cart c WHERE c.user = :user", Cart.class).setParameter(
                    "user",
                    user
            ).getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }
}
