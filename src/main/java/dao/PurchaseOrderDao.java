package dao;

import entities.PurchaseOrder;
import jakarta.persistence.EntityManager;

/**
 * Data Access Object (DAO) for PurchaseOrder entity.
 */
public class PurchaseOrderDao extends GenericDao<PurchaseOrder> {

    /**
     * Constructor.
     *
     * @param em JPA EntityManager.
     */
    public PurchaseOrderDao(EntityManager em) {
        super(PurchaseOrder.class, em);
    }
}
