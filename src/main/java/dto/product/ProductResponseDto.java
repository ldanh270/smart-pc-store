package dto.product;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object for product response data. Includes product details,
 * related entity info (supplier, category), and computed stock status. Returned
 * by all product retrieval/modification endpoints.
 */
public class ProductResponseDto {

    public UUID id;
    public String productName;
    public String description;
    public String imageUrl;
    public BigDecimal currentPrice;
    public Integer quantity;

    public UUID supplierId;
    public String supplierName;

    public UUID categoryId;
    public String categoryName;

    public Boolean status;
    public String stockStatus;
}
