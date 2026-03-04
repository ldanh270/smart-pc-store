package dao;

import java.util.List;

import entities.OrderDetail;
import jakarta.persistence.TypedQuery;

/**
 * OrderDetailDao - Provides DB queries specific to OrderDetail entity
 */
public class OrderDetailDao extends GenericDao<OrderDetail> {

    public OrderDetailDao() {
        super(OrderDetail.class);
    }

    /**
     * Find order details by order ID
     *
     * @param orderId
     * @return List of order details with the given order ID
     */
    public List<OrderDetail> findByOrderId(Integer orderId) {
        String jpql = "SELECT od FROM OrderDetail od WHERE od.order.id = :orderId";
        TypedQuery<OrderDetail> query = getEntityManager().createQuery(jpql, OrderDetail.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }
}
