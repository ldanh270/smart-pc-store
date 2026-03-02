package dto.category;

import dto.product.ProductResponseDto;
import java.util.List;

/**
 * Data Transfer Object for category responses. Contains the category details
 * returned to the client.
 */
public class CategoryResponseDto {

    public Integer id;
    public String categoryName;
    public String description;
    public String imageUrl;
    public Boolean status;
    public Integer parentId;
    public List<ProductResponseDto> products;
}
