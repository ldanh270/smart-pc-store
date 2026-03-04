package dao;

import entities.Order;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class OrderDAO extends GenericDao<Order> {
    public OrderDAO() {
        super(Order.class);
    }

    public List<Order> findByOrderCode(String orderCode) {
        String jpql = "SELECT o FROM Order o WHERE o.orderCode = :orderCode";
        TypedQuery<Order> query = getEntityManager().createQuery(jpql, Order.class);
        query.setParameter("orderCode", orderCode);
        return query.getResultList();
    }

    public Order findSingleByOrderCode(String orderCode) {
        List<Order> list = findByOrderCode(orderCode);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Order> findAllSorted() {
        String jpql = "SELECT o FROM Order o ORDER BY o.createdAt DESC";
        return getEntityManager().createQuery(jpql, Order.class).getResultList();
    }

    public Order findSingleByTransactionCode(String transactionCode) {
        String jpql = "SELECT o FROM Order o WHERE o.transactionCode = :transactionCode";
        TypedQuery<Order> query = getEntityManager().createQuery(jpql, Order.class);
        query.setParameter("transactionCode", transactionCode);
        List<Order> list = query.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}
