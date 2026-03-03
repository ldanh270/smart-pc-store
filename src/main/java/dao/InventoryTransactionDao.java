package dao;

import entities.InventoryTransaction;

/**
 * Data Access Object (DAO) for InventoryTransaction entity.
 */
public class InventoryTransactionDao extends GenericDao<InventoryTransaction> {
    public InventoryTransactionDao() {
        super(InventoryTransaction.class);
    }
}
