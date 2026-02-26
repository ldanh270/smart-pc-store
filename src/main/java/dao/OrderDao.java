package dao;

import entities.Order;
import jakarta.persistence.EntityManager;

public class OrderDao extends GenericDao<Order, Integer> {

    public OrderDao(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected Class<Order> getEntityClass() {
        return Order.class;
    }
}
