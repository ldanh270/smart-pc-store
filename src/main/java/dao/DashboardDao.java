package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.List;
import java.time.OffsetDateTime;

import dto.dashboard.DashboardTopProductDto;

/**
 * DAO for dashboard aggregate queries.
 */
public class DashboardDao {

    private EntityManager getEntityManager() {
        return JPAUtil.getEntityManager();
    }

    // ── Revenue ──────────────────────────────────────────────────────────

    /**
     * Sum of order amounts with status = 'PAID' between two dates.
     */
    public BigDecimal getTotalRevenue(OffsetDateTime from, OffsetDateTime to) {
        String jpql = "SELECT COALESCE(SUM(o.amount), 0) FROM Order o " +
                "WHERE o.status = 'PAID' AND o.createdAt >= :from AND o.createdAt < :to";
        TypedQuery<Double> q = getEntityManager().createQuery(jpql, Double.class);
        q.setParameter("from", from);
        q.setParameter("to", to);
        Double result = q.getSingleResult();
        return BigDecimal.valueOf(result != null ? result : 0);
    }

    // ── Orders ───────────────────────────────────────────────────────────

    /**
     * Count of orders created between two dates.
     */
    public Long countOrders(OffsetDateTime from, OffsetDateTime to) {
        String jpql = "SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from AND o.createdAt < :to";
        TypedQuery<Long> q = getEntityManager().createQuery(jpql, Long.class);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getSingleResult();
    }

    // ── Customers ────────────────────────────────────────────────────────

    /**
     * Count of users (customers) registered between two dates.
     */
    public Long countNewCustomers(OffsetDateTime from, OffsetDateTime to) {
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.createdAt >= :from AND u.createdAt < :to";
        TypedQuery<Long> q = getEntityManager().createQuery(jpql, Long.class);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getSingleResult();
    }

    // ── Products Sold ────────────────────────────────────────────────────

    /**
     * Sum of quantities sold (from OrderDetail) for orders with status = 'PAID' between two dates.
     */
    public Long countProductsSold(OffsetDateTime from, OffsetDateTime to) {
        String jpql = "SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od " +
                "WHERE od.order.status = 'PAID' AND od.order.createdAt >= :from AND od.order.createdAt < :to";
        TypedQuery<Long> q = getEntityManager().createQuery(jpql, Long.class);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getSingleResult();
    }

    /**
     * Count active products grouped by category name.
     * Returns rows in shape: [categoryName (String|null), productCount (Long)].
     */
    public List<Object[]> getProductCountByCategory() {
        String jpql = "SELECT c.categoryName, COUNT(p) " +
                "FROM Product p LEFT JOIN p.category c " +
                "WHERE p.status = true " +
                "GROUP BY c.categoryName " +
                "ORDER BY COUNT(p) DESC";

        Query q = getEntityManager().createQuery(jpql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = q.getResultList();
        return results;
    }

    // ── Top Products ─────────────────────────────────────────────────────

    /**
     * Get top selling products based on total quantity sold in paid orders.
     */
    public List<DashboardTopProductDto> getTopSellingProducts(int limit) {
        String jpql = "SELECT new dto.dashboard.DashboardTopProductDto(" +
                "p.id, p.productName, p.slug, p.imageUrl, p.currentPrice, SUM(od.quantity)) " +
                "FROM OrderDetail od " +
                "JOIN od.order o " +
                "JOIN od.product p " +
                "WHERE o.status = 'PAID' " +
                "GROUP BY p.id, p.productName, p.slug, p.imageUrl, p.currentPrice " +
                "ORDER BY SUM(od.quantity) DESC";

        TypedQuery<DashboardTopProductDto> q = getEntityManager()
                .createQuery(jpql, DashboardTopProductDto.class);
        q.setMaxResults(limit);
        return q.getResultList();
    }
}
