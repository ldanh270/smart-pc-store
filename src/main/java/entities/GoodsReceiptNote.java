package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * GoodsReceiptNote entity.
 * Represents one receiving document linked to a purchase order.
 */
@Entity
@Table(name = "GoodsReceiptNotes")
public class GoodsReceiptNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PoId", nullable = false)
    private PurchaseOrder po;

    @Column(name = "ReceiptDate", nullable = false)
    private LocalDate receiptDate;

    @Nationalized
    @Column(name = "Note")
    private String note;
    @OneToMany(mappedBy = "grn")
    private Set<GoodsReceiptNoteItem> goodsReceiptNoteItems = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PurchaseOrder getPo() {
        return po;
    }

    public void setPo(PurchaseOrder po) {
        this.po = po;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDate receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Set<GoodsReceiptNoteItem> getGoodsReceiptNoteItems() {
        return goodsReceiptNoteItems;
    }

    public void setGoodsReceiptNoteItems(Set<GoodsReceiptNoteItem> goodsReceiptNoteItems) {
        this.goodsReceiptNoteItems = goodsReceiptNoteItems;
    }
}
