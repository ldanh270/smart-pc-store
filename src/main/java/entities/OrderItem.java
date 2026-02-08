package entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderItems")
public class OrderItem {
    @Id
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId")
    private Product product;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "UnitPrice", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

}