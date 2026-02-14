package dao;

import entities.Cart;
import entities.CartItem;
import entities.Product;
import jakarta.persistence.EntityManager;

public class CartItemDao extends GenericDao<CartItem> {
    public CartItemDao(EntityManager em) {
        super(CartItem.class, em);
    }

    public CartItem findByCartAndProduct(Cart cart, Product product) {
        try {
            return em.createQuery(
                            "SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.product = :product",
                            CartItem.class
                    ).setParameter("cart", cart)
                     .setParameter("product", product)
                     .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }
}
