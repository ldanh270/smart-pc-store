package dao;

import java.util.List;

import entities.PurchaseOrderItem;

/**
 * Data Access Object (DAO) for PurchaseOrderItem entity.
 */
public class PurchaseOrderItemDao extends GenericDao<PurchaseOrderItem> {

    public PurchaseOrderItemDao() {
        super(PurchaseOrderItem.class);
    }

    /**
     * Retrieve all line items of a purchase order.
     *
     * @param poId Purchase order ID.
     * @return Item list of the purchase order.
     */
    public List<PurchaseOrderItem> findByPoId(Integer poId) {
        String jpql = "SELECT i FROM PurchaseOrderItem i WHERE i.po.id = :poId";
        return JPAUtil.getEntityManager()
                .createQuery(jpql, PurchaseOrderItem.class)
                .setParameter("poId", poId)
                .getResultList();
    }
}
