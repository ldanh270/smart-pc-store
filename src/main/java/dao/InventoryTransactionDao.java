package dao;

import entities.InventoryTransaction;
import jakarta.persistence.EntityManager;

/**
 * Data Access Object (DAO) for InventoryTransaction entity.
 */
public class InventoryTransactionDao extends GenericDao<InventoryTransaction> {

    /**
     * Constructor.
     *
     * @param em JPA EntityManager.
     */
    public InventoryTransactionDao(EntityManager em) {
        super(InventoryTransaction.class, em);
    }
}
