package entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
// import java.util.List; // If there are OrderItems

@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    // Other order fields...
    // private User user;
    // private String status; // E.g., PENDING, PAID, SHIPPED, DELIVERED, CANCELLED

    @Column(name = "TotalAmount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount; // Total amount of the order

    @Column(name = "CreatedAt", nullable = false)
    private Instant createdAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public BigDecimal getTotalAmount() {
        // In a real application, this method might calculate the sum from OrderItems
        // For simplicity, assume it's stored directly or calculated here.
        return totalAmount;
    }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // TODO: Add other necessary fields and methods for Order entity
    // E.g.: getStatus(), setStatus(), getUser(), setUser(), getOrderItems(), setOrderItems()
}
