package dao;

import entities.GoodsReceiptNoteItem;

/**
 * Data Access Object (DAO) for GoodsReceiptNoteItem entity. Supports aggregate
 * queries for received quantities.
 */
public class GoodsReceiptNoteItemDao extends GenericDao<GoodsReceiptNoteItem> {

    public GoodsReceiptNoteItemDao() {
        super(GoodsReceiptNoteItem.class);
    }

    /**
     * Sum all received quantities for one PO line identified by PO + product.
     *
     * @param poId      Purchase order ID.
     * @param productId Product ID.
     * @return Total received quantity.
     */
    public int sumReceivedQuantityByPoAndProduct(Integer poId, Integer productId) {
        String jpql = "SELECT COALESCE(SUM(i.quantityReceived), 0) " + "FROM GoodsReceiptNoteItem i " + "WHERE i.grn.po.id = :poId AND i.product.id = :productId";
        Long value = JPAUtil.getEntityManager().createQuery(jpql, Long.class).setParameter("poId", poId).setParameter("productId",
                                                                                                                      productId
        ).getSingleResult();
        return value == null ? 0 : value.intValue();
    }
}
