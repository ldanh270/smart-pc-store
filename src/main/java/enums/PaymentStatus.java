package enums;

/**
 * Enum định nghĩa các trạng thái có thể có của một giao dịch thanh toán.
 */
public enum PaymentStatus {
    PENDING,    // Đang chờ xử lý hoặc chưa hoàn tất
    COMPLETED,  // Thanh toán thành công
    FAILED,     // Thanh toán thất bại
    CANCELLED,  // Thanh toán đã bị hủy
    REFUNDED    // Thanh toán đã được hoàn lại
}
