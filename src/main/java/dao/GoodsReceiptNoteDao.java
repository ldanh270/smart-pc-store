package dao;

import entities.GoodsReceiptNote;
import jakarta.persistence.EntityManager;

/**
 * Data Access Object (DAO) for GoodsReceiptNote entity.
 */
public class GoodsReceiptNoteDao extends GenericDao<GoodsReceiptNote> {

    /**
     * Constructor.
     *
     * @param em JPA EntityManager.
     */
    public GoodsReceiptNoteDao(EntityManager em) {
        super(GoodsReceiptNote.class, em);
    }
}
