package services;

import dao.ProductDao;
import dto.product.ProductRequestDto;
import dto.product.ProductResponseDto;
// import dao.SupplierDao;
// import dao.CategoryDao;
import entities.Product;
// import entities.Supplier;
// import entities.Category;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service class for handling product management operations.
 * Manages CRUD operations, search/filter, stock adjustments, and DTO conversions.
 */
public class ProductService {

    private final ProductDao productDao;
    // private final SupplierDao supplierDao;
    // private final CategoryDao categoryDao;
    private final EntityManager em;

    public ProductService(ProductDao productDao,
                        //   SupplierDao supplierDao,
                        //   CategoryDao categoryDao,
                          EntityManager em) {
        this.productDao = productDao;
        // this.supplierDao = supplierDao;
        // this.categoryDao = categoryDao;
        this.em = em;
    }

    /**
     * Retrieve all products from the database.
     *
     * @return A list of all products.
     */
    public List<Product> getAll() {
        return productDao.findAll();
    }

    /**
     * Retrieve a product by its ID.
     *
     * @param id The product ID.
     * @return The product if found, otherwise null.
     */
    public Product getById(Integer id) {
        return productDao.findById(id);
    }

    /**
     * Create a new product with validation.
     * Validates product name, price, quantity, and references (supplier, category).
     *
     * @param dto The product request DTO containing product details.
     * @return The created product entity.
     * @throws IllegalArgumentException if validation fails or references are invalid.
     */
    public Product create(ProductRequestDto dto) {

        if (dto.productName == null || dto.productName.isBlank())
            throw new IllegalArgumentException("Product name is required");

        if (dto.currentPrice == null || dto.currentPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Price must be greater than 0");

        if (dto.quantity == null || dto.quantity < 0)
            throw new IllegalArgumentException("Quantity must be >= 0");

        if (dto.supplierId == null)
            throw new IllegalArgumentException("Supplier is required");

        if (dto.categoryId == null)
            throw new IllegalArgumentException("Category is required");

        var supplier = em.find(entities.Supplier.class, dto.supplierId);
        if (supplier == null)
            throw new IllegalArgumentException("Supplier not found");

        var category = em.find(entities.Category.class, dto.categoryId);
        if (category == null)
            throw new IllegalArgumentException("Category not found");

        Product product = new Product();
        product.setProductName(dto.productName);
        product.setDescription(dto.description);
        if (dto.imageUrl != null) product.setImageUrl(dto.imageUrl);
        product.setCurrentPrice(dto.currentPrice);
        product.setQuantity(dto.quantity);
        if (dto.status != null) product.setStatus(dto.status);
        product.setSupplier(supplier);
        product.setCategory(category);

        try {
            em.getTransaction().begin();
            productDao.create(product);
            em.getTransaction().commit();
            return product;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public ProductResponseDto toDto(Product p) {

        ProductResponseDto dto = new ProductResponseDto();
        dto.id = p.getId();
        dto.productName = p.getProductName();
        dto.description = p.getDescription();
        dto.currentPrice = p.getCurrentPrice();
        dto.quantity = p.getQuantity();
        dto.imageUrl = p.getImageUrl();

        dto.status = p.getStatus();
        // Determine stock status based on quantity
        int qty = p.getQuantity() == null ? 0 : p.getQuantity();
        if (qty <= 0) dto.stockStatus = "Out of stock";
        else if (qty <= 5) dto.stockStatus = "Low stock";
        else dto.stockStatus = "In stock";

        dto.supplierId = p.getSupplier().getId();
        dto.supplierName = p.getSupplier().getSupplierName();

        dto.categoryId = p.getCategory().getId();
        dto.categoryName = p.getCategory().getCategoryName();

        return dto;
    }

    /**
     * Convert a list of products to a list of response DTOs.
     *
     * @param list The list of product entities.
     * @return A list of ProductResponseDto objects.
     */
    public List<ProductResponseDto> toDtoList(List<Product> list) {
        return list.stream().map(this::toDto).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Search and filter products with multiple criteria.
     *
     * @param categoryId The category ID filter (optional).
     * @param status The product status filter (optional).
     * @param minPrice The minimum price filter (optional).
     * @param maxPrice The maximum price filter (optional).
     * @param keyword The product name keyword search (optional).
     * @param page The page number for pagination (optional).
     * @param size The page size for pagination (optional).
     * @return A list of filtered ProductResponseDto objects.
     */
    public List<ProductResponseDto> searchWithFilters(Integer categoryId, Boolean status,
                                                      java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                                                      String keyword, Integer page, Integer size) {
        List<Product> list = productDao.filterSearch(categoryId, status, minPrice, maxPrice, keyword, page, size);
        return toDtoList(list);
    }

    /**
     * Retrieve all products as response DTOs.
     *
     * @return A list of all ProductResponseDto objects.
     */
    public List<ProductResponseDto> getAllDtos() {
        return toDtoList(getAll());
    }

    /**
     * Retrieve a product by ID as a response DTO.
     *
     * @param id The product ID.
     * @return The ProductResponseDto if found, otherwise null.
     */
    public ProductResponseDto getByIdDto(Integer id) {
        Product p = getById(id);
        return p == null ? null : toDto(p);
    }

    /**
     * Update an existing product with field-level validation.
     * Only updates allowed fields (name, description, image, price, quantity, status).
     * Does not modify supplier or category references.
     *
     * @param product The product entity with updates.
     * @return The updated product entity.
     * @throws IllegalArgumentException if product not found or validation fails.
     */
    public Product update(Product product) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("Product id is required for update");
        }

        Product existing = productDao.findById(product.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Product not found");
        }
        // Merge allowed fields only (do not change supplier/category here)
        if (product.getProductName() != null) {
            if (product.getProductName().isBlank()) throw new IllegalArgumentException("Product name is required");
            existing.setProductName(product.getProductName());
        }

        if (product.getDescription() != null) existing.setDescription(product.getDescription());
        if (product.getImageUrl() != null) existing.setImageUrl(product.getImageUrl());

        if (product.getCurrentPrice() != null) {
            if (product.getCurrentPrice().compareTo(java.math.BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Price must be greater than 0");
            existing.setCurrentPrice(product.getCurrentPrice());
        }

        if (product.getQuantity() != null) {
            if (product.getQuantity() < 0) throw new IllegalArgumentException("Quantity must be >= 0");
            existing.setQuantity(product.getQuantity());
        }

        if (product.getStatus() != null) existing.setStatus(product.getStatus());

        try {
            em.getTransaction().begin();
            Product merged = productDao.update(existing);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Delete a product by its ID.
     *
     * @param id The product ID.
     * @throws IllegalArgumentException if product ID is null.
     */
    public void delete(Integer id) {
        if (id == null) throw new IllegalArgumentException("Product id is required");
        try {
            em.getTransaction().begin();
            productDao.delete(id);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Adjust product stock by a delta quantity (positive to increase, negative to decrease).
     * Validates that resulting quantity is not negative.
     *
     * @param id The product ID.
     * @param delta The quantity change (can be positive or negative).
     * @return The updated ProductResponseDto with new stock status.
     * @throws IllegalArgumentException if product not found, resulting quantity is negative, or ID is null.
     */
    public ProductResponseDto adjustStock(Integer id, int delta) {
        if (id == null) throw new IllegalArgumentException("Product id is required");
        Product existing = productDao.findById(id);
        if (existing == null) throw new IllegalArgumentException("Product not found");

        int current = existing.getQuantity() == null ? 0 : existing.getQuantity();
        int updated = current + delta;
        if (updated < 0) throw new IllegalArgumentException("Resulting quantity cannot be negative");

        existing.setQuantity(updated);

        try {
            em.getTransaction().begin();
            Product merged = productDao.update(existing);
            em.getTransaction().commit();
            return toDto(merged);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

}
