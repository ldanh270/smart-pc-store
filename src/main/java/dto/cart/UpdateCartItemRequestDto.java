package dto.cart;

/**
 * Request body for PUT /cart/items/{id}
 */
public class UpdateCartItemRequestDto {

    /** New quantity (if <= 0, item will be removed) */
    private Integer quantity;

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}