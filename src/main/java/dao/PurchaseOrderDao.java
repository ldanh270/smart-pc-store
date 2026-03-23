package dao;

import entities.PurchaseOrder;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

/**
 * PurchaseOrderDao - Data Access Object for purchase orders
 * PurchaseOrderDao - 仕入注文のデータアクセスオブジェクト
 */
public class PurchaseOrderDao extends GenericDao<PurchaseOrder> {

    public PurchaseOrderDao() {
        super(PurchaseOrder.class);
    }

    /**
     * Get adjustment orders associated with the original order
     * 元の注文に関連付けられた調整注文を取得する
     *
     * @param parentId ID of the original order
     * @return List of adjustment orders
     */
    public List<PurchaseOrder> findAdjustmentsByParentId(UUID parentId) {
        String jpql = "SELECT po FROM PurchaseOrder po WHERE po.parentOrder.id = :parentId";
        TypedQuery<PurchaseOrder> query = getEntityManager().createQuery(jpql, PurchaseOrder.class);
        query.setParameter("parentId", parentId);
        return query.getResultList();
    }

    /**
     * Search and paginate purchase orders.
     *
     * @param q    Keyword for search.
     * @param page Zero-based page index.
     * @param size Page size.
     * @return Matching purchase orders.
     */
    public List<PurchaseOrder> searchAndPaginate(String q, Integer page, Integer size) {
        StringBuilder jpql = new StringBuilder("SELECT po FROM PurchaseOrder po");
        boolean hasQ = (q != null && !q.isBlank());

        if (hasQ) {
            jpql.append(" WHERE CAST(po.id AS string) LIKE :q");
            jpql.append(" OR po.type LIKE :q");
        }

        jpql.append(" ORDER BY po.orderDate DESC, po.id DESC");
        TypedQuery<PurchaseOrder> query = getEntityManager().createQuery(jpql.toString(), PurchaseOrder.class);

        if (hasQ) {
            query.setParameter("q", "%" + q + "%");
        }

        if (page != null && size != null) {
            query.setFirstResult(page * size);
            query.setMaxResults(size);
        }

        return query.getResultList();
    }
}
