package dto.cart;

import java.math.BigDecimal;

public class CartItemResponseDto {
    private Integer cartItemId;
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer stockQuantity;

    public CartItemResponseDto(Integer cartItemId, Integer productId, String productName,
            BigDecimal price, Integer quantity, Integer stockQuantity) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.stockQuantity = stockQuantity;
        // Null-check để tránh NullPointerException khi price chưa được set
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
