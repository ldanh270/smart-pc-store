package dto.cart;

import java.util.UUID;

/**
 * Request body for POST /cart/add
 */
public class AddToCartRequestDto {

    /**
     * Product id to add to cart
     */
    private UUID productId;

    /**
     * Quantity to add (must be > 0)
     */
    private Integer quantity;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
