package controllers;

import java.io.IOException;
import java.util.List;

import dto.ApiResponse;
import dto.category.CategoryRequestDto;
import dto.category.CategoryResponseDto;
import entities.Category;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.CategoryService;
import utils.HttpUtil;
import utils.validate.AuthValidate;
import utils.validate.CategoryValidate;

/**
 * Controller class for handling HTTP requests related to category management.
 * Handles category CRUD operations.
 */
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Handle GET request to retrieve all categories. Query parameters: q
     * (keyword)
     */
    public void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String q = req.getParameter("q");

        List<CategoryResponseDto> categories = categoryService.search(q);
        HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, categories);
    }

    /**
     * Handle GET request to retrieve a single category by ID.
     */
    public void handleGetById(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            CategoryResponseDto category = categoryService.getByIdDto(id);

            if (category == null) {
                HttpUtil.sendJson(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        new ApiResponse<>(false, "Category not found", null)
                );
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, category);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    new ApiResponse<>(false, "Invalid category id", null)
            );
        }
    }

    /**
     * Handle POST request to create a new category.
     */
    public void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CategoryRequestDto dto = HttpUtil.jsonToClass(req.getReader(), CategoryRequestDto.class);

            // Validate input data
            List<String> validationError = CategoryValidate.validateCreateCategory(dto);

            // Return 400 Bad Request if validation fails
            if (!validationError.isEmpty()) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, validationError);
                return;
            }

            Category category = categoryService.create(dto);

            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_CREATED,
                    new ApiResponse<>(true, "Category created successfully", categoryService.toDto(category))
            );

        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiResponse<>(false, e.getMessage(), null));

        } catch (Exception e) {
            System.err.println("ERROR CategoryController - handleCreate: " + e.getMessage());
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ApiResponse<>(false, "Internal server error", e.getMessage())
            );
        }
    }

    /**
     * Handle PUT request to update an existing category.
     */
    public void handleUpdate(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            CategoryRequestDto dto = HttpUtil.jsonToClass(req.getReader(), CategoryRequestDto.class);

            var updated = categoryService.update(id, dto);
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    new ApiResponse<>(true, "Category updated successfully", categoryService.toDto(updated))
            );

        } catch (NumberFormatException e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    new ApiResponse<>(false, "Invalid category id", null)
            );
        } catch (IllegalArgumentException e) {
            int status = "Category not found".equals(e.getMessage()) ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            HttpUtil.sendJson(resp, status, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }

    /**
     * Handle DELETE request to soft-delete a category.
     */
    public void handleDelete(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            categoryService.delete(id);
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    new ApiResponse<>(true, "Category deleted successfully", null)
            );
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    new ApiResponse<>(false, "Invalid category id", null)
            );
        } catch (IllegalArgumentException e) {
            int status = "Category not found".equals(e.getMessage()) ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            HttpUtil.sendJson(resp, status, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }
}
