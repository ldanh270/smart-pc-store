package dao;

import entities.Cart;
import entities.CartItem;
import entities.Product;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * CartItemDao
 * - Provides DB queries specific to CartItem entity
 */
public class CartItemDao extends GenericDao<CartItem> {
    public CartItemDao() {
        super(CartItem.class);
    }

    /**
     * Find cart item by cart + product (used to detect duplicates in cart).
     *
     * @return CartItem if found, otherwise null
     */
    public CartItem findByCartAndProduct(Cart cart, Product product) {
        try {
            return getEntityManager().createQuery("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.product = :product",
                                                  CartItem.class
                    )
                    .setParameter("cart", cart)
                    .setParameter("product", product)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }

    /**
     * Get all cart items by cart and fetch product eagerly.
     * Using JOIN FETCH avoids LazyInitializationException outside transaction.
     */
    public List<CartItem> findByCartWithProduct(Cart cart) {
        return getEntityManager().createQuery(
                "SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart = :cart",
                CartItem.class
        ).setParameter("cart", cart).getResultList();
    }
}
