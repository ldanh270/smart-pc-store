package dto.cart;

import java.math.BigDecimal;

/**
 * Response DTO for cart items (GET /cart).
 *
 * Fields:
 * - cartItemId: id of CartItem row
 * - productId/productName/price: product info for UI
 * - quantity: quantity in cart
 * - subtotal: price * quantity (computed)
 * - stockQuantity: current stock (used by FE to limit and show warnings)
 */
public class CartItemResponseDto {

    private Integer cartItemId;
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;

    /**
     * Derived field: price * quantity
     * Computed in constructor for convenience.
     */
    private BigDecimal subtotal;

    /**
     * Stock quantity at the time of response.
     * FE can use this value to show max quantity / out-of-stock warning.
     */
    private Integer stockQuantity;

    public CartItemResponseDto(Integer cartItemId, Integer productId, String productName,
                               BigDecimal price, Integer quantity, Integer stockQuantity) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.stockQuantity = stockQuantity;

        // Defensive null-check to avoid NullPointerException if price is missing
        this.subtotal = (price != null)
                ? price.multiply(BigDecimal.valueOf(quantity))
                : BigDecimal.ZERO;
    }

    public Integer getCartItemId() {
        return cartItemId;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }
}