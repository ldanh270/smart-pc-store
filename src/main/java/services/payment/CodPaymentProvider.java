package services.payment;

import dao.PaymentDao;
import entities.Order;
import entities.Payment;
import enums.PaymentMethod;
import enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment provider for Cash On Delivery (COD).
 * For COD, the payment is typically marked as PENDING initially and then COMPLETED upon delivery.
 * This provider simulates the initiation and callback (which would be manual in a real scenario).
 */
public class CodPaymentProvider implements PaymentProvider {

    private final PaymentDao paymentDao;

    public CodPaymentProvider(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    @Override
    public PaymentMethod getPaymentMethodType() {
        return PaymentMethod.COD;
    }

    @Override
    public Payment initiatePayment(Order order, BigDecimal amount) throws PaymentException {
        // For COD, we immediately create a PENDING payment record.
        // The actual "completion" happens offline (at delivery).
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.COD);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING); // Initial status for COD
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        try {
            paymentDao.save(payment); // Save the payment to the DB
            return payment;
        } catch (Exception e) {
            throw new PaymentException("Failed to initiate COD payment for order " + order.getId(), e);
        }
    }

    @Override
    public Payment processCallback(Integer paymentId, Object callbackData) throws PaymentException {
        // For COD, a "callback" is typically a manual update by an admin
        // indicating the delivery and collection were successful.
        // This method can be used to simulate that or be called by an internal system.
        Payment payment = paymentDao.findById(paymentId)
                .orElseThrow(() -> new PaymentException("COD Payment not found for ID: " + paymentId));

        // In a real scenario, callbackData might contain confirmation from the delivery system.
        // For this simulation, we assume a successful "callback" means the payment is completed.
        if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setUpdatedAt(Instant.now());
            try {
                paymentDao.update(payment);
                // TODO: Update the order status to PAID or PROCESSING
                return payment;
            } catch (Exception e) {
                throw new PaymentException("Failed to process COD payment callback for ID: " + paymentId, e);
            }
        } else {
            throw new PaymentException("COD Payment ID " + paymentId + " is not in PENDING state for callback.");
        }
    }

    @Override
    public PaymentStatus getStatus(Payment payment) throws PaymentException {
        // For COD, the status is simply what's stored in our database.
        // There's no external gateway to query.
        return payment.getPaymentStatus();
    }
}
