package services.payment;

import entities.Order;
import entities.Payment;
import enums.PaymentMethod;
import enums.PaymentStatus;

import java.math.BigDecimal;

/**
 * Interface for different payment method providers.
 * Each provider will handle the specific logic for initiating and processing a payment
 * for a given payment method.
 */
public interface PaymentProvider {

    /**
     * Returns the payment method type this provider handles.
     * @return The PaymentMethod enum value.
     */
    PaymentMethod getPaymentMethodType();

    /**
     * Initiates a payment for a given order.
     * This method should create a new Payment record in the database with a PENDING status
     * and return any necessary information to the client (e.g., a redirect URL for external gateways).
     *
     * @param order The order for which the payment is being initiated.
     * @param amount The total amount to be paid.
     * @return A Payment object representing the initiated transaction.
     * @throws PaymentException if the payment initiation fails.
     */
    Payment initiatePayment(Order order, BigDecimal amount) throws PaymentException;

    /**
     * Processes a payment callback/webhook from a payment gateway.
     * This method should validate the callback data, update the Payment status,
     * and potentially update the Order status.
     *
     * @param paymentId The ID of the payment to be updated.
     * @param callbackData An object containing the data from the gateway's callback.
     * @return The updated Payment object.
     * @throws PaymentException if processing the callback fails (e.g., invalid signature, payment not found).
     */
    Payment processCallback(Integer paymentId, Object callbackData) throws PaymentException;

    /**
     * Gets the current status of a payment from the payment gateway (if applicable).
     * For internal methods like COD, this might just return the stored status.
     *
     * @param payment The payment object to check the status for.
     * @return The current PaymentStatus.
     * @throws PaymentException if there's an issue communicating with the gateway.
     */
    PaymentStatus getStatus(Payment payment) throws PaymentException;
}
