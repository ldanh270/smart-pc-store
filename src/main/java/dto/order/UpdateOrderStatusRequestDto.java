package dto.order;

/**
 * Request DTO for updating order status.
 */
public class UpdateOrderStatusRequestDto {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
