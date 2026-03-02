package dao;

import entities.PurchaseOrderItem;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Data Access Object (DAO) for PurchaseOrderItem entity.
 */
public class PurchaseOrderItemDao extends GenericDao<PurchaseOrderItem> {

    /**
     * Constructor.
     *
     * @param em JPA EntityManager.
     */
    public PurchaseOrderItemDao(EntityManager em) {
        super(PurchaseOrderItem.class, em);
    }

    /**
     * Retrieve all line items of a purchase order.
     *
     * @param poId Purchase order ID.
     * @return Item list of the purchase order.
     */
    public List<PurchaseOrderItem> findByPoId(Integer poId) {
        String jpql = "SELECT i FROM PurchaseOrderItem i WHERE i.po.id = :poId";
        return em.createQuery(jpql, PurchaseOrderItem.class)
                .setParameter("poId", poId)
                .getResultList();
    }
}
