package dao;

import entities.PurchaseOrder;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

/**
 * PurchaseOrderDao - 仕入注文のデータアクセスオブジェクト
 */
public class PurchaseOrderDao extends GenericDao<PurchaseOrder> {

    public PurchaseOrderDao() {
        super(PurchaseOrder.class);
    }

    /**
     * 元の注文に関連付けられた調整注文を取得する
     *
     * @param parentId 元の注文ID
     * @return 調整注文のリスト
     */
    public List<PurchaseOrder> findAdjustmentsByParentId(UUID parentId) {
        String jpql = "SELECT po FROM PurchaseOrder po WHERE po.parentOrder.id = :parentId";
        TypedQuery<PurchaseOrder> query = getEntityManager().createQuery(jpql, PurchaseOrder.class);
        query.setParameter("parentId", parentId);
        return query.getResultList();
    }
}
