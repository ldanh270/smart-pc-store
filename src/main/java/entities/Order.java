package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId")
    private User user;

    @Column(name = "OrderDate")
    private Instant orderDate;

    @Nationalized
    @Column(name = "Status")
    private String status;

    @Column(name = "TotalAmount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

}