package services;

import dao.OrderDAO;
import dto.payment.PaymentResponseDto;
import entities.Order;
import utils.NumberUtil;

public class PaymentService {
    private final OrderDAO orderDao;

    public PaymentService(OrderDAO orderDao) {
        this.orderDao = orderDao;
    }

    public PaymentResponseDto getQrInfo(String identifier) {
        Order order = null;
        if (NumberUtil.isNumeric(identifier)) {
            order = orderDao.findById(Integer.parseInt(identifier));
        } else {
            order = orderDao.findSingleByOrderCode(identifier);
        }

        if (order == null) return null;

        String qrUrl = String.format(
                "https://qr.sepay.vn/img?acc=VQRQAELYF2308&bank=MBBank&amount=%.0f&des=%s",
                order.getAmount(), order.getTransactionCode()
        );

        return new PaymentResponseDto(order.getAmount(), order.getTransactionCode(), qrUrl);
    }

    public void updateStatus(String transactionCode, String status) {
        Order order = orderDao.findSingleByTransactionCode(transactionCode);
        if (order != null) {
            try {
                orderDao.getEntityManager().getTransaction().begin();
                order.setStatus(status);
                orderDao.update(order);
                orderDao.getEntityManager().getTransaction().commit();
            } catch (Exception e) {
                if (orderDao.getEntityManager().getTransaction().isActive()) {
                    orderDao.getEntityManager().getTransaction().rollback();
                }
                throw e;
            }
        }
    }
}
