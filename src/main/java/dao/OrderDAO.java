package dao;

import java.util.List;

import entities.Order;
import jakarta.persistence.TypedQuery;

/**
 * OrderDAO - Provides DB queries specific to Order entity
 *
 */
public class OrderDAO extends GenericDao<Order> {

    public OrderDAO() {
        super(Order.class);
    }

    /**
     * Find orders by order code
     *
     * @param orderCode
     * @return List of orders with the given order code
     */
    public List<Order> findByOrderCode(String orderCode) {
        String jpql = "SELECT o FROM Order o WHERE o.orderCode = :orderCode";
        TypedQuery<Order> query = getEntityManager().createQuery(jpql, Order.class);
        query.setParameter("orderCode", orderCode);
        return query.getResultList();
    }

    /**
     * Find single order by order code
     *
     * @param orderCode
     * @return Single order with the given order code
     */
    public Order findSingleByOrderCode(String orderCode) {
        List<Order> list = findByOrderCode(orderCode);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Find all orders sorted by creation date
     *
     * @return List of orders sorted by creation date
     */
    public List<Order> findAllSorted() {
        String jpql = "SELECT o FROM Order o ORDER BY o.createdAt DESC";
        return getEntityManager().createQuery(jpql, Order.class).getResultList();
    }

    /**
     * Find single order by transaction code
     *
     * @param transactionCode
     * @return Single order with the given transaction code
     */
    public Order findSingleByTransactionCode(String transactionCode) {
        String jpql = "SELECT o FROM Order o WHERE o.transactionCode = :transactionCode";
        TypedQuery<Order> query = getEntityManager().createQuery(jpql, Order.class);
        query.setParameter("transactionCode", transactionCode);
        List<Order> list = query.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}
