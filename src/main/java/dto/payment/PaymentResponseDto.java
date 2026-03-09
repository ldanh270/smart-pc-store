package dto.payment;

public class PaymentResponseDto {

    private Double amount;
    private String transactionCode;
    private String qrUrl;

    public PaymentResponseDto() {
    }

    public PaymentResponseDto(Double amount, String transactionCode, String qrUrl) {
        this.amount = amount;
        this.transactionCode = transactionCode;
        this.qrUrl = qrUrl;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }
}
