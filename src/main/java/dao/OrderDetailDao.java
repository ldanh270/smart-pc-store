package dao;

import entities.OrderDetail;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class OrderDetailDao extends GenericDao<OrderDetail> {
    public OrderDetailDao() {
        super(OrderDetail.class);
    }

    public List<OrderDetail> findByOrderId(Integer orderId) {
        String jpql = "SELECT od FROM OrderDetail od WHERE od.order.id = :orderId";
        TypedQuery<OrderDetail> query = getEntityManager().createQuery(jpql, OrderDetail.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }
}
