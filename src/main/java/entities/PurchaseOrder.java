package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "PurchaseOrders")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplierId")
    private Supplier supplier;

    @Column(name = "OrderDate")
    private LocalDate orderDate;

    @Column(name = "ExpectedDeliveryDate")
    private LocalDate expectedDeliveryDate;

    @Nationalized
    @Column(name = "Status")
    private String status;

    @Nationalized
    @Column(name = "PoCode")
    private String poCode;
    @OneToMany(mappedBy = "po")
    private Set<GoodsReceiptNote> goodsReceiptNotes = new LinkedHashSet<>();
    @OneToMany(mappedBy = "po")
    private Set<PurchaseOrderItem> purchaseOrderItems = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getPoCode() {
        return poCode;
    }

    public void setPoCode(String poCode) {
        this.poCode = poCode;
    }

    public Set<GoodsReceiptNote> getGoodsReceiptNotes() {
        return goodsReceiptNotes;
    }

    public void setGoodsReceiptNotes(Set<GoodsReceiptNote> goodsReceiptNotes) {
        this.goodsReceiptNotes = goodsReceiptNotes;
    }

    public Set<PurchaseOrderItem> getPurchaseOrderItems() {
        return purchaseOrderItems;
    }

    public void setPurchaseOrderItems(Set<PurchaseOrderItem> purchaseOrderItems) {
        this.purchaseOrderItems = purchaseOrderItems;
    }

}
