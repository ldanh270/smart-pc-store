package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "RevenueDaily")
public class RevenueDaily {
    @Id
    @Column(name = "RevenueDate", nullable = false)
    private LocalDate id;

    @Column(name = "TotalRevenue", precision = 18, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "TotalOrders")
    private Integer totalOrders;

    public LocalDate getId() {
        return id;
    }

    public void setId(LocalDate id) {
        this.id = id;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

}
