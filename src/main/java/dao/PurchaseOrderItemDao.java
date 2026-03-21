package dao;

import entities.PurchaseOrderItem;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

/**
 * PurchaseOrderItemDao - 仕入注文明細のデータアクセスオブジェクト
 */
public class PurchaseOrderItemDao extends GenericDao<PurchaseOrderItem> {

    public PurchaseOrderItemDao() {
        super(PurchaseOrderItem.class);
    }

    /**
     * 注文IDに関連するすべての明細を取得する
     *
     * @param poId 注文ID
     * @return 注文明細のリスト
     */
    public List<PurchaseOrderItem> findByPurchaseOrderId(UUID poId) {
        String jpql = "SELECT poi FROM PurchaseOrderItem poi WHERE poi.po.id = :poId";
        TypedQuery<PurchaseOrderItem> query = getEntityManager().createQuery(jpql, PurchaseOrderItem.class);
        query.setParameter("poId", poId);
        return query.getResultList();
    }

    /**
     * 特定の製品と注文（およびその調整注文）に関連するすべての明細を取得する
     *
     * @param productId 製品ID
     * @param poId 元の注文ID
     * @return 明細のリスト
     */
    public List<PurchaseOrderItem> findByProductAndOrderFamily(UUID productId, UUID poId) {
        String jpql = "SELECT poi FROM PurchaseOrderItem poi WHERE poi.product.id = :productId " +
                      "AND (poi.po.id = :poId OR poi.po.parentOrder.id = :poId)";
        TypedQuery<PurchaseOrderItem> query = getEntityManager().createQuery(jpql, PurchaseOrderItem.class);
        query.setParameter("productId", productId);
        query.setParameter("poId", poId);
        return query.getResultList();
    }
}
