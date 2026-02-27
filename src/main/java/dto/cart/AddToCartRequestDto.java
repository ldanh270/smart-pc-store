package dto.cart;

/**
 * Request body for POST /cart/add
 */
public class AddToCartRequestDto {

    /** Product id to add to cart */
    private Integer productId;

    /** Quantity to add (must be > 0) */
    private Integer quantity;

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}