package entities;

import java.sql.Timestamp;

public class OrderModel {
    private int id;
    private String orderCode;
    private double amount;
    private String transactionCode;
    private String status;
    private Timestamp createdAt;

    public OrderModel() {
    }

    public OrderModel(int id, String orderCode, double amount, String transactionCode, String status, Timestamp createdAt) {
        this.id = id;
        this.orderCode = orderCode;
        this.amount = amount;
        this.transactionCode = transactionCode;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
