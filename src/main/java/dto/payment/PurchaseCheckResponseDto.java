package dto.payment;

public class PurchaseCheckResponseDto {

    private String transactionCode;
    private String status;
    private boolean completed;
    private String message;

    public PurchaseCheckResponseDto() {
    }

    public PurchaseCheckResponseDto(String transactionCode, String status, boolean completed, String message) {
        this.transactionCode = transactionCode;
        this.status = status;
        this.completed = completed;
        this.message = message;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
