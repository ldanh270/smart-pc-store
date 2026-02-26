package services;

import dao.OrderDao;
import dao.PaymentDao;
import entities.Order;
import entities.Payment;
import enums.PaymentMethod;
import enums.PaymentStatus;
import services.payment.PaymentException;
import services.payment.PaymentProvider;
import services.payment.CodPaymentProvider;
// import services.payment.PaypalPaymentProvider; // Example for the future

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class to handle payment-related business logic.
 * Uses the Strategy Pattern to delegate payment processing to specific PaymentProviders.
 */
public class PaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    private final PaymentDao paymentDao;
    private final OrderDao orderDao; // To fetch order details
    private final Map<PaymentMethod, PaymentProvider> paymentProviders;

    public PaymentService(PaymentDao paymentDao, OrderDao orderDao) {
        this.paymentDao = paymentDao;
        this.orderDao = orderDao;
        this.paymentProviders = new HashMap<>();
        // Register payment providers
        registerPaymentProvider(new CodPaymentProvider(paymentDao));
        // registerPaymentProvider(new PaypalPaymentProvider(paymentDao, paypalConfig)); // Example
        // registerPaymentProvider(new BankTransferPaymentProvider(paymentDao)); // Example
    }

    private void registerPaymentProvider(PaymentProvider provider) {
        paymentProviders.put(provider.getPaymentMethodType(), provider);
        LOGGER.log(Level.INFO, "Registered PaymentProvider for: {0}", provider.getPaymentMethodType());
    }

    /**
     * Initiates a payment for a given order using the specified payment method.
     *
     * @param orderId The ID of the order to be paid.
     * @param paymentMethod The chosen payment method.
     * @return The initiated Payment object.
     * @throws PaymentException if the order is not found, invalid, or payment initiation fails.
     */
    public Payment initiatePayment(Integer orderId, PaymentMethod paymentMethod) throws PaymentException {
        Optional<Order> orderOptional = orderDao.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new PaymentException("Order not found with ID: " + orderId);
        }
        Order order = orderOptional.get();

        // TODO: Add logic to check if order is already paid or in a non-payable state
        // if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CANCELLED) {
        //     throw new PaymentException("Order " + orderId + " cannot be paid.");
        // }

        PaymentProvider provider = paymentProviders.get(paymentMethod);
        if (provider == null) {
            throw new PaymentException("Unsupported payment method: " + paymentMethod);
        }

        // Assuming the Order entity has a getTotalAmount() method
        BigDecimal amount = order.getTotalAmount(); // Need to ensure Order entity has this method

        LOGGER.log(Level.INFO, "Initiating payment for Order ID: {0} with Method: {1}, Amount: {2}",
                new Object[]{orderId, paymentMethod, amount});
        return provider.initiatePayment(order, amount);
    }

    /**
     * Gets the status of a payment.
     *
     * @param paymentId The ID of the payment.
     * @return The Payment object with its current status.
     * @throws PaymentException if the payment is not found.
     */
    public Payment getPaymentStatus(Integer paymentId) throws PaymentException {
        Optional<Payment> paymentOptional = paymentDao.findById(paymentId);
        if (paymentOptional.isEmpty()) {
            throw new PaymentException("Payment not found with ID: " + paymentId);
        }
        Payment payment = paymentOptional.get();

        // For external gateways, we might want to query the gateway for the latest status
        // PaymentProvider provider = paymentProviders.get(payment.getPaymentMethod());
        // if (provider != null && provider.requiresExternalStatusCheck()) { // Need to add this flag to PaymentProvider
        //     payment.setPaymentStatus(provider.getStatus(payment));
        //     paymentDao.update(payment); // Update if status changed
        // }
        LOGGER.log(Level.INFO, "Retrieved payment status for ID: {0}, Status: {1}",
                new Object[]{payment.getId(), payment.getPaymentStatus()});
        return payment;
    }

    /**
     * Processes a callback from a payment gateway.
     *
     * @param paymentId The ID of the payment.
     * @param paymentMethod The payment method associated with the callback.
     * @param callbackData The data received from the gateway.
     * @return The updated Payment object.
     * @throws PaymentException if processing fails.
     */
    public Payment processPaymentCallback(Integer paymentId, PaymentMethod paymentMethod, Object callbackData) throws PaymentException {
        PaymentProvider provider = paymentProviders.get(paymentMethod);
        if (provider == null) {
            throw new PaymentException("Unsupported payment method for callback: " + paymentMethod);
        }
        LOGGER.log(Level.INFO, "Processing callback for Payment ID: {0} with Method: {1}",
                new Object[]{paymentId, paymentMethod});
        Payment updatedPayment = provider.processCallback(paymentId, callbackData);

        // TODO: After successful payment, update the order status to 'PAID' or 'PROCESSING'
        // Order order = updatedPayment.getOrder();
        // if (updatedPayment.getPaymentStatus() == PaymentStatus.COMPLETED) {
        //     order.setStatus(OrderStatus.PAID); // Need to define OrderStatus and setStatus method
        //     orderDao.update(order);
        // }

        return updatedPayment;
    }
}
