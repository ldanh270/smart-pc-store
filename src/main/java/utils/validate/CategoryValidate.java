package utils.validate;

import dto.category.CategoryRequestDto;

import java.util.ArrayList;
import java.util.List;

public class CategoryValidate {
    public static List<String> validateCreateCategory(CategoryRequestDto dto) {
        // List to hold validation error messages
        List<String> errors = new ArrayList<>();

        if (dto == null) {
            errors.add("Request body must not empty");
            return errors;
        }

        if (dto.categoryName == null || dto.categoryName.isBlank()) {
            errors.add("Category name is required");
        } else if (dto.categoryName.length() > 255) {
            errors.add("Category name must not exceed 255 characters");
        }

        if (dto.description != null && dto.description.length() > 1000) {
            errors.add("Description must not exceed 1000 characters");
        }

        // Return list of errors
        return errors;
    }
}
