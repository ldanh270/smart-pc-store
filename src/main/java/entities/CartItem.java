package entities;

import jakarta.persistence.*;

/**
 * CartItem entity
 *
 * Table: CartItems
 * - Represents one product line in a cart
 * - Quantity is the number of units of the product in cart
 */
@Entity
@Table(name = "CartItems")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    /**
     * Owning cart of this item.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CartId")
    private Cart cart;

    /**
     * Product being added into cart.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId")
    private Product product;

    /**
     * Quantity of this product in the cart.
     */
    @Column(name = "Quantity")
    private Integer quantity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}