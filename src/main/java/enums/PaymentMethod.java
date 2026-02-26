package enums;

/**
 * Enum định nghĩa các phương thức thanh toán được hỗ trợ.
 * Dễ dàng mở rộng bằng cách thêm các giá trị mới.
 */
public enum PaymentMethod {
    COD,           // Cash On Delivery
    BANK_TRANSFER, // Chuyển khoản ngân hàng
    PAYPAL,        // Cổng thanh toán PayPal
    STRIPE,        // Cổng thanh toán Stripe
    MOMO           // Ví điện tử MoMo (ví dụ)
}
