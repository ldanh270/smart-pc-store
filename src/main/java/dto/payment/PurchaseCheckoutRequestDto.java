package dto.payment;

import java.util.List;
import java.util.UUID;

public class PurchaseCheckoutRequestDto {

    private UUID userId;
    private UserRefDto user;
    private List<PurchaseProductDto> products;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UserRefDto getUser() {
        return user;
    }

    public void setUser(UserRefDto user) {
        this.user = user;
    }

    public List<PurchaseProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<PurchaseProductDto> products) {
        this.products = products;
    }

    public UUID resolveUserId() {
        if (userId != null) {
            return userId;
        }
        return user == null ? null : user.getId();
    }

    public static class UserRefDto {
        private UUID id;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }
    }

    public static class PurchaseProductDto {
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
