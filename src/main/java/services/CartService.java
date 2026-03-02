package services;

import dao.CartDao;
import dao.CartItemDao;
import dao.GenericDao;
import dao.UserDao;
import dto.cart.CartItemResponseDto;
import entities.*;

import java.util.List;

/**
 * CartService
 *
 * Responsibilities:
 * - Implement cart business logic
 * - Validate user/cart/cartItem ownership
 * - Validate stock rules (when adding/updating)
 * - Perform DB changes in transactions
 *
 * Important:
 * - This service uses DAO's EntityManager transaction boundaries explicitly.
 * - Any exception inside transaction will rollback and rethrow.
 */
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

    /**
     * Get cart items for current user.
     *
     * Current behavior:
     * - If user has no cart => return empty list
     * - Does NOT enforce stock conflicts on GET (it only returns stockQuantity for FE to show limits)
     */
    public List<CartItemResponseDto> getMyCart(Integer userId) {
        User user = userDao.findById(userId);
        if (user == null)
            throw new RuntimeException("User not found");

        Cart cart = cartDao.findByUser(user);
        if (cart == null)
            return List.of();

        // Use JOIN FETCH to avoid LazyInitializationException when accessing product fields
        List<CartItem> items = cartItemDao.findByCartWithProduct(cart);

        return items.stream()
                .map(ci -> new CartItemResponseDto(
                        ci.getId(),
                        ci.getProduct().getId(),
                        ci.getProduct().getProductName(),
                        ci.getProduct().getCurrentPrice(), // use current price for display
                        ci.getQuantity(),
                        ci.getProduct().getQuantity() // stockQuantity for frontend constraints/UX
                ))
                .toList();
    }

    /**
     * Add product to cart.
     *
     * Validation:
     * - productId must not be null
     * - quantity must be > 0
     * - user must exist
     * - product must exist
     * - if product quantity (stock) is tracked, requested qty must not exceed stock
     *
     * Behavior:
     * - If cart not exists => create it
     * - If item already exists => increase quantity
     * - Else => create new item
     */
    public void addToCart(Integer userId, Integer productId, Integer quantity) {
        // Validate input early (avoid unnecessary DB calls)
        if (productId == null)
            throw new RuntimeException("Product ID is required");
        if (quantity == null || quantity <= 0)
            throw new RuntimeException("Quantity must be > 0");

        User user = userDao.findById(userId);
        Product product = productDao.findById(productId);

        if (user == null)
            throw new RuntimeException("User not found");
        if (product == null)
            throw new RuntimeException("Product not found");

        // Stock validation (if stock is not null)
        if (product.getQuantity() != null && product.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }

        try {
            cartDao.getEntityManager().getTransaction().begin();

            // Create cart for user if not exists
            Cart cart = cartDao.findByUser(user);
            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
                cartDao.create(cart);
            }

            // If product already in cart => increase quantity
            CartItem item = cartItemDao.findByCartAndProduct(cart, product);
            if (item != null) {
                int newQty = item.getQuantity() + quantity;

                // Re-check stock for the new total quantity
                if (product.getQuantity() != null && product.getQuantity() < newQty) {
                    throw new RuntimeException("Not enough stock");
                }

                item.setQuantity(newQty);
                cartItemDao.update(item);
            } else {
                // Create a new cart item
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                cartItemDao.create(newItem);
            }

            cartDao.getEntityManager().getTransaction().commit();
        } catch (Exception e) {
            // Rollback if something fails mid-transaction
            if (cartDao.getEntityManager().getTransaction().isActive()) {
                cartDao.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Update quantity of a cart item.
     *
     * Validation:
     * - quantity must not be null
     * - user must exist
     * - cart must exist
     * - cartItem must exist AND must belong to current user's cart
     * - if quantity > 0, it must not exceed product stock (when stock is tracked)
     *
     * Behavior:
     * - quantity <= 0 => delete cart item
     * - quantity > 0  => update cart item quantity
     */
    public void updateQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        if (quantity == null)
            throw new RuntimeException("Quantity is required");

        User user = userDao.findById(userId);
        if (user == null)
            throw new RuntimeException("User not found");

        Cart cart = cartDao.findByUser(user);
        if (cart == null)
            throw new RuntimeException("Cart not found");

        // Ensure cart item exists and belongs to this user's cart
        CartItem item = cartItemDao.findById(cartItemId);
        if (item == null || item.getCart() == null || !item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item not found");
        }

        // Stock validation for positive quantity updates
        Product product = item.getProduct();
        if (quantity > 0 && product != null && product.getQuantity() != null && product.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }

        try {
            cartDao.getEntityManager().getTransaction().begin();

            if (quantity <= 0) {
                // Treat non-positive quantity as "remove item"
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

    /**
     * Remove a cart item (helper method).
     * Internally it uses updateQuantity(quantity=0).
     */
    public void removeItem(Integer userId, Integer cartItemId) {
        updateQuantity(userId, cartItemId, 0);
    }

    /**
     * Clear the entire cart.
     * Typically called after checkout is completed successfully.
     *
     * Behavior:
     * - If user/cart does not exist => do nothing
     * - Delete all items in cart (one by one)
     */
    public void clearCart(Integer userId) {
        User user = userDao.findById(userId);
        if (user == null)
            throw new RuntimeException("User not found");

        Cart cart = cartDao.findByUser(user);
        if (cart == null)
            return; // No cart => nothing to clear

        List<CartItem> items = cartItemDao.findByCartWithProduct(cart);
        if (items.isEmpty())
            return;

        try {
            cartDao.getEntityManager().getTransaction().begin();
            for (CartItem item : items) {
                cartItemDao.delete(item.getId());
            }
            cartDao.getEntityManager().getTransaction().commit();
        } catch (Exception e) {
            if (cartDao.getEntityManager().getTransaction().isActive()) {
                cartDao.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }
}