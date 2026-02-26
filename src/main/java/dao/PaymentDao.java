package dao;

import entities.Payment;
import jakarta.persistence.EntityManager;

public class PaymentDao extends GenericDao<Payment, Integer> {

    public PaymentDao(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected Class<Payment> getEntityClass() {
        return Payment.class;
    }
}
