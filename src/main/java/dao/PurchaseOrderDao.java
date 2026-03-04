package dao;

import entities.PurchaseOrder;

/**
 * Data Access Object (DAO) for PurchaseOrder entity.
 */
public class PurchaseOrderDao extends GenericDao<PurchaseOrder> {

    public PurchaseOrderDao() {
        super(PurchaseOrder.class);
    }
}
