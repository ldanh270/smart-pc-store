package dao;

import entities.GoodsReceiptNote;

/**
 * Data Access Object (DAO) for GoodsReceiptNote entity.
 */
public class GoodsReceiptNoteDao extends GenericDao<GoodsReceiptNote> {
    public GoodsReceiptNoteDao() {
        super(GoodsReceiptNote.class);
    }
}
