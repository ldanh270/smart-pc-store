package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Entity
@Table(name = "Payments")
public class Payment {
    @Id
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Column(name = "OrderId")
    private Integer orderId;

    @Nationalized
    @Column(name = "PaymentMethod")
    private String paymentMethod;

    @Nationalized
    @Column(name = "PaymentStatus")
    private String paymentStatus;

    @Column(name = "PaymentDate")
    private Instant paymentDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }

}