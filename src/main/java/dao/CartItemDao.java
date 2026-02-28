package dao;

import entities.Cart;
import entities.CartItem;
import entities.Product;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CartItemDao extends GenericDao<CartItem> {
    public CartItemDao() {
        super(CartItem.class);
    }

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

    public List<CartItem> findByCartWithProduct(Cart cart) {
        return getEntityManager().createQuery(
                "SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart = :cart",
                CartItem.class
        ).setParameter("cart", cart).getResultList();
    }
}
