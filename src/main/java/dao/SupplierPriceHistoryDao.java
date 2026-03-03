package dao;

import entities.SupplierPriceHistory;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object (DAO) for SupplierPriceHistory entity.
 * Supports quotation history retrieval and latest-price lookups.
 */
public class SupplierPriceHistoryDao extends GenericDao<SupplierPriceHistory> {
    public SupplierPriceHistoryDao() {
        super(SupplierPriceHistory.class);
    }

    /**
     * Get full quotation history for a product from a specific supplier.
     *
     * @param productId Product ID.
     * @param supplierId Supplier ID.
     * @return Ordered quotation history by effective date ascending.
     */
    public List<SupplierPriceHistory> findByProductAndSupplier(Integer productId, Integer supplierId) {
        String jpql = "SELECT h FROM SupplierPriceHistory h " +
                "WHERE h.product.id = :productId AND h.supplier.id = :supplierId " +
                "ORDER BY h.effectiveDate ASC";
        return JPAUtil.getEntityManager().createQuery(jpql, SupplierPriceHistory.class)
                .setParameter("productId", productId)
                .setParameter("supplierId", supplierId)
                .getResultList();
    }

    /**
     * Get latest quotation for product + supplier.
     *
     * @param productId Product ID.
     * @param supplierId Supplier ID.
     * @return Latest quotation or null if none exists.
     */
    public SupplierPriceHistory findLatest(Integer productId, Integer supplierId) {
        String jpql = "SELECT h FROM SupplierPriceHistory h " +
                "WHERE h.product.id = :productId AND h.supplier.id = :supplierId " +
                "ORDER BY h.effectiveDate DESC, h.id DESC";
        List<SupplierPriceHistory> list = JPAUtil.getEntityManager().createQuery(jpql, SupplierPriceHistory.class)
                .setParameter("productId", productId)
                .setParameter("supplierId", supplierId)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Get latest quotation per supplier for one product.
     *
     * @param productId Product ID.
     * @return Latest quotation records sorted by import price ascending.
     */
    public List<SupplierPriceHistory> findLatestByProduct(Integer productId) {
        String jpql = "SELECT h FROM SupplierPriceHistory h " +
                "WHERE h.product.id = :productId " +
                "AND h.effectiveDate = (" +
                "  SELECT MAX(h2.effectiveDate) FROM SupplierPriceHistory h2 " +
                "  WHERE h2.product.id = :productId AND h2.supplier.id = h.supplier.id" +
                ") " +
                "ORDER BY h.importPrice ASC";
        TypedQuery<SupplierPriceHistory> query = JPAUtil.getEntityManager().createQuery(jpql, SupplierPriceHistory.class);
        query.setParameter("productId", productId);
        return query.getResultList();
    }
}
