package controllers;

import entities.Product;
import services.ProductService;
import utils.HttpUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import dto.ApiResponse;
import dto.product.ProductRequestDto;
import dto.product.ProductResponseDto;

/**
 * Controller class for handling HTTP requests related to product management.
 * Handles product CRUD operations, search/filter, and stock adjustments.
 */
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Handle GET request to retrieve all products with optional filtering.
     * Query parameters: q (keyword), categoryId, status, minPrice, maxPrice, page, size
     *
     * @param req The HTTP request containing filter parameters.
     * @param resp The HTTP response to send product data.
     * @throws IOException if I/O error occurs.
     */
    public void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // read filter params
        String q = req.getParameter("q");
        String cat = req.getParameter("categoryId");
        String st = req.getParameter("status");
        String min = req.getParameter("minPrice");
        String max = req.getParameter("maxPrice");
        String pageStr = req.getParameter("page");
        String sizeStr = req.getParameter("size");

        Integer categoryId = (cat == null || cat.isBlank()) ? null : Integer.parseInt(cat);
        Boolean status = (st == null || st.isBlank()) ? null : Boolean.parseBoolean(st);
        java.math.BigDecimal minPrice = (min == null || min.isBlank()) ? null : new java.math.BigDecimal(min);
        java.math.BigDecimal maxPrice = (max == null || max.isBlank()) ? null : new java.math.BigDecimal(max);
        Integer page = (pageStr == null || pageStr.isBlank()) ? null : Integer.parseInt(pageStr);
        Integer size = (sizeStr == null || sizeStr.isBlank()) ? null : Integer.parseInt(sizeStr);

        List<ProductResponseDto> products = productService.searchWithFilters(categoryId, status, minPrice, maxPrice, q, page, size);
        HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, products);
    }

    /**
     * Handle GET request to retrieve a product by ID.
     *
     * @param req The HTTP request.
     * @param resp The HTTP response to send product details.
     * @param idStr The product ID as a string.
     * @throws IOException if I/O error occurs.
     */
    public void handleGetById(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            ProductResponseDto product = productService.getByIdDto(id);

            if (product == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Product not found");
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, product);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid product id");
        }
    }

    /**
     * Handle POST request to create a new product.
     * Request body must contain ProductRequestDto as JSON.
     *
     * @param req The HTTP request containing product data JSON.
     * @param resp The HTTP response to send creation result.
     * @throws IOException if I/O error occurs.
     */
    public void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ProductRequestDto dto = HttpUtil.jsonToClass(req.getReader(), ProductRequestDto.class);

                Product product = productService.create(dto);

                HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED,
                    new ApiResponse<>(true, "Product created successfully", productService.toDto(product)));

        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new ApiResponse<>(false, e.getMessage(), null));

        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(false, "Internal server error", null));
        }
    }

    /**
     * Handle PUT request to update an existing product.
     * Request body must contain Product JSON. Supplier and category cannot be modified via this endpoint.
     *
     * @param req The HTTP request containing updated product data JSON.
     * @param resp The HTTP response to send update result.
     * @param idStr The product ID as a string.
     * @throws IOException if I/O error occurs.
     */
    public void handleUpdate(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            Product product = HttpUtil.jsonToClass(req.getReader(), Product.class);
            product.setId(id);

            Product updated = productService.update(product);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, productService.toDto(updated));

        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid product id");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Handle DELETE request to delete a product.
     *
     * @param req The HTTP request.
     * @param resp The HTTP response to send deletion result.
     * @param idStr The product ID as a string.
     * @throws IOException if I/O error occurs.
     */
    public void handleDelete(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            productService.delete(id);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Product deleted successfully");
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid product id");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Handle PUT request to adjust product stock by a delta quantity.
     * Request body must contain AdjustStockRequest JSON with delta field.
     *
     * @param req The HTTP request containing delta in JSON format.
     * @param resp The HTTP response to send adjustment result.
     * @param idStr The product ID as a string.
     * @throws IOException if I/O error occurs.
     */
    public void handleAdjustStock(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            dto.product.AdjustStockRequest adj = HttpUtil.jsonToClass(req.getReader(), dto.product.AdjustStockRequest.class);
            if (adj == null || adj.delta == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiResponse<>(false, "delta is required", null));
                return;
            }

            ProductResponseDto updated = productService.adjustStock(id, adj.delta);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, new ApiResponse<>(true, "Stock adjusted", updated));

        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiResponse<>(false, "Invalid product id", null));
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiResponse<>(false, "Internal server error", null));
        }
    }
}
