/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author ducan
 */
@Entity
@Table(name = "PurchaseOrderItems")
@NamedQueries({
    @NamedQuery(name = "PurchaseOrderItem.findAll", query = "SELECT p FROM PurchaseOrderItem p"),
    @NamedQuery(name = "PurchaseOrderItem.findById", query = "SELECT p FROM PurchaseOrderItem p WHERE p.id = :id"),
    @NamedQuery(name = "PurchaseOrderItem.findByQuantity", query = "SELECT p FROM PurchaseOrderItem p WHERE p.quantity = :quantity"),
    @NamedQuery(name = "PurchaseOrderItem.findByUnitPrice", query = "SELECT p FROM PurchaseOrderItem p WHERE p.unitPrice = :unitPrice")})
public class PurchaseOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    private Integer id;
    @Column(name = "Quantity")
    private Integer quantity;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "UnitPrice")
    private BigDecimal unitPrice;
    @JoinColumn(name = "ProductId", referencedColumnName = "Id")
    @ManyToOne
    private Product productId;
    @JoinColumn(name = "PoId", referencedColumnName = "Id")
    @ManyToOne
    private PurchaseOrder poId;

    public PurchaseOrderItem() {
    }

    public PurchaseOrderItem(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Product getProductId() {
        return productId;
    }

    public void setProductId(Product productId) {
        this.productId = productId;
    }

    public PurchaseOrder getPoId() {
        return poId;
    }

    public void setPoId(PurchaseOrder poId) {
        this.poId = poId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PurchaseOrderItem)) {
            return false;
        }
        PurchaseOrderItem other = (PurchaseOrderItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.PurchaseOrderItem[ id=" + id + " ]";
    }
    
}
