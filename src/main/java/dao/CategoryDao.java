package dao;

import java.util.List;

import entities.Category;
import jakarta.persistence.TypedQuery;

/**
 * Data Access Object (DAO) class for Category entity. Extends GenericDao to
 * provide CRUD operations and custom category-specific queries.
 */
public class CategoryDao extends GenericDao<Category> {

    public CategoryDao() {
        super(Category.class);
    }

    /**
     * Search categories by keyword (case-insensitive partial match). Only
     * returns active categories by default.
     *
     * @param keyword The search keyword (null or blank returns all active
     * categories).
     * @return A list of active categories matching the keyword.
     */
    public List<Category> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAllActive();
        }
        String jpql = "SELECT c FROM Category c WHERE c.status = true AND LOWER(c.categoryName) LIKE LOWER(:kw)";
        return JPAUtil.getEntityManager()
                .createQuery(jpql, Category.class)
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
    }

    /**
     * Retrieve all active categories (status = true).
     *
     * @return A list of active categories.
     */
    public List<Category> findAllActive() {
        String jpql = "SELECT c FROM Category c WHERE c.status = true";
        return JPAUtil.getEntityManager().createQuery(jpql, Category.class).getResultList();
    }

    /**
     * Retrieve all categories with pagination support. Calculates offset based
     * on page number and size.
     *
     * @param page The zero-based page number.
     * @param size The number of records per page.
     * @return A paginated list of categories.
     */
    public List<Category> findWithPagination(int page, int size) {
        String jpql = "SELECT c FROM Category c WHERE c.status = true";
        return JPAUtil.getEntityManager().createQuery(jpql, Category.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Count all active categories.
     *
     * @return The total number of active categories.
     */
    public long countAllActive() {
        String jpql = "SELECT COUNT(c) FROM Category c WHERE c.status = true";
        return JPAUtil.getEntityManager().createQuery(jpql, Long.class).getSingleResult();
    }

    /**
     * Check if a category name already exists (case-insensitive).
     *
     * @param name The category name to check.
     * @param excludeId Category ID to exclude (for update scenarios), can be
     * null.
     * @return true if a duplicate exists.
     */
    public boolean existsByName(String name, Integer excludeId) {
        String jpql = "SELECT COUNT(c) FROM Category c WHERE LOWER(c.categoryName) = LOWER(:name) AND c.status = true";
        if (excludeId != null) {
            jpql += " AND c.id <> :excludeId";
        }
        TypedQuery<Long> query = JPAUtil.getEntityManager().createQuery(jpql, Long.class);
        query.setParameter("name", name);
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        return query.getSingleResult() > 0;
    }
}
