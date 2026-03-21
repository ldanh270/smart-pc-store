package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "\"PurchaseOrders\"")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "\"supplierId\"")
    private Supplier supplier;

    @Column(name = "\"orderDate\"")
    private LocalDate orderDate;

    /**
     * 注文タイプ (NORMAL: 通常, ADJUSTMENT: 調整)
     */
    @Column(name = "type", length = 20)
    private String type = "NORMAL";

    /**
     * 元の注文への参照 (調整注文の場合のみ sử dụng)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"parentOrderId\"")
    private PurchaseOrder parentOrder;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PurchaseOrder getParentOrder() {
        return parentOrder;
    }

    public void setParentOrder(PurchaseOrder parentOrder) {
        this.parentOrder = parentOrder;
    }

}
