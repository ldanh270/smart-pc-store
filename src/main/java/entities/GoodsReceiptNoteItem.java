package entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * GoodsReceiptNoteItem entity.
 * Represents a received line item in one goods receipt note.
 */
@Entity
@Table(name = "GoodsReceiptNoteItems")
public class GoodsReceiptNoteItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GrnId", nullable = false)
    private GoodsReceiptNote grn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Column(name = "QuantityReceived", nullable = false)
    private Integer quantityReceived;

    @Column(name = "UnitCost", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitCost;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public GoodsReceiptNote getGrn() {
        return grn;
    }

    public void setGrn(GoodsReceiptNote grn) {
        this.grn = grn;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
