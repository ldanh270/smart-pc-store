package dao;

import entities.PurchaseOrder;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object (DAO) for PurchaseOrder entity.
 */
public class PurchaseOrderDao extends GenericDao<PurchaseOrder> {

    public PurchaseOrderDao() {
        super(PurchaseOrder.class);
    }

    /**
     * Search and paginate purchase orders.
     *
     * @param query The search keyword (matches supplier name or PO ID).
     * @param page  The zero-based page number.
     * @param size  The number of records per page.
     * @return A list of matching PurchaseOrder entities.
     */
    public List<PurchaseOrder> searchAndPaginate(String query, Integer page, Integer size) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM PurchaseOrder p");
        
        if (query != null && !query.isBlank()) {
            jpql.append(" JOIN p.supplier s WHERE LOWER(s.supplierName) LIKE LOWER(:query) OR CAST(p.id AS string) LIKE LOWER(:query)");
        }
        jpql.append(" ORDER BY p.orderDate DESC");
        
        TypedQuery<PurchaseOrder> tQuery = getEntityManager().createQuery(jpql.toString(), PurchaseOrder.class);
        
        if (query != null && !query.isBlank()) {
            tQuery.setParameter("query", "%" + query + "%");
        }
        
        if (page != null && size != null) {
            tQuery.setFirstResult((page - 1) * size);
            tQuery.setMaxResults(size);
        }
        
        return tQuery.getResultList();
    }
}
