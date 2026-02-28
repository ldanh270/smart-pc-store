package dao;

import entities.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object (DAO) class for Product entity.
 * Extends GenericDao to provide CRUD operations and custom product-specific queries.
 */
public class ProductDao extends GenericDao<Product> {

    public ProductDao() {
        super(Product.class);
    }

    /**
     * Find all products belonging to a specific category.
     *
     * @param categoryId The category ID.
     * @return A list of products in the specified category.
     */
    public List<Product> findByCategoryId(Integer categoryId) {
        String jpql = "SELECT p FROM Product p WHERE p.category.id = :categoryId";
        TypedQuery<Product> query = JPAUtil.getEntityManager().createQuery(jpql, Product.class);
        query.setParameter("categoryId", categoryId);
        return query.getResultList();
    }

    /**
     * Find all products from a specific supplier.
     *
     * @param supplierId The supplier ID.
     * @return A list of products supplied by the specified supplier.
     */
    public List<Product> findBySupplierId(Integer supplierId) {
        String jpql = "SELECT p FROM Product p WHERE p.supplier.id = :supplierId";
        TypedQuery<Product> query = JPAUtil.getEntityManager().createQuery(jpql, Product.class);
        query.setParameter("supplierId", supplierId);
        return query.getResultList();
    }

    /**
     * Search products by product name using partial/fuzzy matching (case-insensitive).
     * Uses LIKE operator for flexible matching.
     *
     * @param keyword The search keyword.
     * @return A list of products matching the keyword.
     */
    public List<Product> searchByName(String keyword) {
        String jpql = "SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(:keyword)";
        TypedQuery<Product> query = JPAUtil.getEntityManager().createQuery(jpql, Product.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }

    /**
     * Retrieve all products with pagination support.
     * Calculates offset based on page number and size.
     *
     * @param page The zero-based page number.
     * @param size The number of records per page.
     * @return A paginated list of products.
     */
    public List<Product> findWithPagination(int page, int size) {
        String jpql = "SELECT p FROM Product p";
        return JPAUtil.getEntityManager().createQuery(jpql, Product.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Search products by keyword (case-insensitive partial match).
     * Simple alt search method.
     *
     * @param keyword The search keyword.
     * @return A list of products matching the keyword.
     */
    public List<Product> search(String keyword) {
        String jpql = "SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(:kw)";
        return JPAUtil.getEntityManager().createQuery(jpql, Product.class)
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
    }

    /**
     * Search and filter products with multiple criteria and pagination support.
     * All filter parameters are optional — only applied if provided (not null).
     * Constructs dynamic JPQL query based on provided filters.
     *
     * @param categoryId The category ID filter (optional).
     * @param status The product status filter (optional).
     * @param minPrice The minimum price threshold (optional).
     * @param maxPrice The maximum price threshold (optional).
     * @param keyword The product name search keyword (optional).
     * @param page The zero-based page number for pagination (optional).
     * @param size The page size for pagination (optional).
     * @return A filtered and paginated list of products.
     */
    public List<Product> filterSearch(Integer categoryId, Boolean status, java.math.BigDecimal minPrice,
                                      java.math.BigDecimal maxPrice, String keyword, Integer page, Integer size) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");

        if (categoryId != null) jpql.append(" AND p.category.id = :categoryId");
        if (status != null) jpql.append(" AND p.status = :status");
        if (minPrice != null) jpql.append(" AND p.currentPrice >= :minPrice");
        if (maxPrice != null) jpql.append(" AND p.currentPrice <= :maxPrice");
        if (keyword != null && !keyword.isBlank()) jpql.append(" AND LOWER(p.productName) LIKE :kw");

        TypedQuery<Product> query = JPAUtil.getEntityManager().createQuery(jpql.toString(), Product.class);

        if (categoryId != null) query.setParameter("categoryId", categoryId);
        if (status != null) query.setParameter("status", status);
        if (minPrice != null) query.setParameter("minPrice", minPrice);
        if (maxPrice != null) query.setParameter("maxPrice", maxPrice);
        if (keyword != null && !keyword.isBlank()) query.setParameter("kw", "%" + keyword.toLowerCase() + "%");

        if (page != null && size != null) {
            query.setFirstResult(page * size);
            query.setMaxResults(size);
        }

        return query.getResultList();
    }
}
