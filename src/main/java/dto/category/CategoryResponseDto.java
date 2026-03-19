package dto.category;

import java.util.List;
import java.util.UUID;

import dto.product.ProductResponseDto;

/**
 * Data Transfer Object for category responses. Contains the category details
 * returned to the client.
 */
public class CategoryResponseDto {

    public UUID id;
    public String categoryName;
    public String slug;
    public String description;
    public String imageUrl;
    public Boolean status;
    public UUID parentId;
    public List<ProductResponseDto> products;
}
