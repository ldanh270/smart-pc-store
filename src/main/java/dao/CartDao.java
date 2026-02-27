package dao;

import entities.Cart;
import entities.User;
import jakarta.persistence.EntityManager;

/**
 * CartDao
 * - Provides DB queries specific to Cart entity
 */
public class CartDao extends GenericDao<Cart> {

    public CartDao(EntityManager em) {
        super(Cart.class, em);
    }

    /**
     * Find a cart by owning user.
     *
     * @param user the owner
     * @return Cart if found, otherwise null (when no cart exists yet)
     */
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