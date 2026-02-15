package dto.product;

import java.math.BigDecimal;

public class ProductResponseDto {

    public Integer id;
    public String productName;
    public String description;
    public String imageUrl;
    public BigDecimal currentPrice;
    public Integer quantity;

    public Integer supplierId;
    public String supplierName;

    public Integer categoryId;
    public String categoryName;

    public Boolean status;
    public String stockStatus;
}
