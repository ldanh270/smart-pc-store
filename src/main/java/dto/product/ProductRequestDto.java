package dto.product;

import java.math.BigDecimal;

/**
 * Data Transfer Object for product creation/update requests.
 * Contains all product information provided by the client.
 * imageUrl and status are optional fields.
 */
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
