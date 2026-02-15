package dto.product;

import java.math.BigDecimal;

public class ProductRequestDto {

    public String productName;
    public String description;
    public String imageUrl;
    public BigDecimal currentPrice;
    public Integer quantity;
    public Integer supplierId;
    public Integer categoryId;
    public Boolean status;
}
