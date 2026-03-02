package dto.category;

/**
 * Data Transfer Object for category creation/update requests. Contains all
 * category information provided by the client.
 */
public class CategoryRequestDto {

    public String categoryName;
    public String description;
    public String imageUrl;
}
