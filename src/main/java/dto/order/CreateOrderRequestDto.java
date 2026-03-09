package dto.order;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for order creation requests.
 */
public class CreateOrderRequestDto {

    private List<OrderItemRequestDto> items;

    public List<OrderItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDto> items) {
        this.items = items;
    }

    public static class OrderItemRequestDto {

        private UUID productId;
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
}
