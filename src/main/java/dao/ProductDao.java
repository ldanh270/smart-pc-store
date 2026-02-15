package dao;

import entities.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ProductDao extends GenericDao<Product> {

    public ProductDao(EntityManager em) {
        super(Product.class, em);
    }

    /**
     * Find products by category id
     */
    public List<Product> findByCategoryId(Integer categoryId) {
        String jpql = "SELECT p FROM Product p WHERE p.category.id = :categoryId";
        TypedQuery<Product> query = em.createQuery(jpql, Product.class);
        query.setParameter("categoryId", categoryId);
        return query.getResultList();
    }

    /**
     * Find products by supplier id
     */
    public List<Product> findBySupplierId(Integer supplierId) {
        String jpql = "SELECT p FROM Product p WHERE p.supplier.id = :supplierId";
        TypedQuery<Product> query = em.createQuery(jpql, Product.class);
        query.setParameter("supplierId", supplierId);
        return query.getResultList();
    }

    /**
     * Search product by name (partial match)
     */
    public List<Product> searchByName(String keyword) {
        String jpql = "SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(:keyword)";
        TypedQuery<Product> query = em.createQuery(jpql, Product.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }

    public List<Product> findWithPagination(int page, int size) {
        String jpql = "SELECT p FROM Product p";
        return em.createQuery(jpql, Product.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Product> search(String keyword) {
        String jpql = "SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(:kw)";
        return em.createQuery(jpql, Product.class)
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
    }

    /**
     * Search and filter products by multiple criteria with pagination
     */
    public List<Product> filterSearch(Integer categoryId, Boolean status, java.math.BigDecimal minPrice,
                                      java.math.BigDecimal maxPrice, String keyword, Integer page, Integer size) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");

        if (categoryId != null) jpql.append(" AND p.category.id = :categoryId");
        if (status != null) jpql.append(" AND p.status = :status");
        if (minPrice != null) jpql.append(" AND p.currentPrice >= :minPrice");
        if (maxPrice != null) jpql.append(" AND p.currentPrice <= :maxPrice");
        if (keyword != null && !keyword.isBlank()) jpql.append(" AND LOWER(p.productName) LIKE :kw");

        TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);

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
