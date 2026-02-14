package services;

import dao.CartDao;
import dao.CartItemDao;
import dao.GenericDao;
import dao.UserDao;
import dto.cart.CartItemResponseDto;
import entities.*;

import java.util.List;

public class CartService {

    private final UserDao userDao;
    private final CartDao cartDao;
    private final CartItemDao cartItemDao;
    private final GenericDao<Product> productDao;

    public CartService(UserDao userDao, CartDao cartDao, CartItemDao cartItemDao, GenericDao<Product> productDao) {
        this.userDao = userDao;
        this.cartDao = cartDao;
        this.cartItemDao = cartItemDao;
        this.productDao = productDao;
    }

    public List<CartItemResponseDto> getMyCart(Integer userId) {
        User user = userDao.findById(userId);
        if (user == null) throw new RuntimeException("User not found");

        Cart cart = cartDao.findByUser(user);
        if (cart == null) return List.of();

        // JOIN FETCH để lấy productName/currentPrice ngay trong khi còn EntityManager
        List<CartItem> items = cartDao.getEntityManager().createQuery(
                "SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart = :cart",
                CartItem.class
        ).setParameter("cart", cart).getResultList();

        return items.stream()
                .map(ci -> new CartItemResponseDto(
                        ci.getId(),
                        ci.getProduct().getId(),
                        ci.getProduct().getProductName(),
                        ci.getProduct().getCurrentPrice(),
                        ci.getQuantity()
                ))
                .toList();
    }

    public void addToCart(Integer userId, Integer productId, Integer quantity) {
        if (quantity == null || quantity <= 0) throw new RuntimeException("Quantity must be > 0");

        User user = userDao.findById(userId);
        Product product = productDao.findById(productId);

        if (user == null) throw new RuntimeException("User not found");
        if (product == null) throw new RuntimeException("Product not found");

        // check tồn kho
        if (product.getQuantity() != null && product.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }

        try {
            cartDao.getEntityManager().getTransaction().begin();

            Cart cart = cartDao.findByUser(user);
            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
                cartDao.create(cart);
            }

            CartItem item = cartItemDao.findByCartAndProduct(cart, product);
            if (item != null) {
                int newQty = item.getQuantity() + quantity;

                if (product.getQuantity() != null && product.getQuantity() < newQty) {
                    throw new RuntimeException("Not enough stock");
                }

                item.setQuantity(newQty);
                cartItemDao.update(item);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                cartItemDao.create(newItem);
            }

            cartDao.getEntityManager().getTransaction().commit();
        } catch (Exception e) {
            if (cartDao.getEntityManager().getTransaction().isActive()) {
                cartDao.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }

    public void updateQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        if (quantity == null) throw new RuntimeException("Quantity is required");

        User user = userDao.findById(userId);
        if (user == null) throw new RuntimeException("User not found");

        Cart cart = cartDao.findByUser(user);
        if (cart == null) throw new RuntimeException("Cart not found");

        CartItem item = cartItemDao.findById(cartItemId);
        if (item == null || item.getCart() == null || !item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item not found");
        }

        Product product = item.getProduct();
        if (quantity > 0 && product != null && product.getQuantity() != null && product.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }

        try {
            cartDao.getEntityManager().getTransaction().begin();

            if (quantity <= 0) {
                cartItemDao.delete(cartItemId);
            } else {
                item.setQuantity(quantity);
                cartItemDao.update(item);
            }

            cartDao.getEntityManager().getTransaction().commit();
        } catch (Exception e) {
            if (cartDao.getEntityManager().getTransaction().isActive()) {
                cartDao.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }

    public void removeItem(Integer userId, Integer cartItemId) {
        updateQuantity(userId, cartItemId, 0);
    }
}
