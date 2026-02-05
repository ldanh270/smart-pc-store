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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author ducan
 */
@Entity
@Table(name = "InventoryTransactions")
@NamedQueries({
    @NamedQuery(name = "InventoryTransaction.findAll", query = "SELECT i FROM InventoryTransaction i"),
    @NamedQuery(name = "InventoryTransaction.findById", query = "SELECT i FROM InventoryTransaction i WHERE i.id = :id"),
    @NamedQuery(name = "InventoryTransaction.findByQuantityChange", query = "SELECT i FROM InventoryTransaction i WHERE i.quantityChange = :quantityChange"),
    @NamedQuery(name = "InventoryTransaction.findByTransactionType", query = "SELECT i FROM InventoryTransaction i WHERE i.transactionType = :transactionType"),
    @NamedQuery(name = "InventoryTransaction.findByTransactionDate", query = "SELECT i FROM InventoryTransaction i WHERE i.transactionDate = :transactionDate")})
public class InventoryTransaction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    private Integer id;
    @Column(name = "QuantityChange")
    private Integer quantityChange;
    @Column(name = "TransactionType")
    private String transactionType;
    @Column(name = "TransactionDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;
    @JoinColumn(name = "ProductId", referencedColumnName = "Id")
    @ManyToOne
    private Product productId;

    public InventoryTransaction() {
    }

    public InventoryTransaction(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Product getProductId() {
        return productId;
    }

    public void setProductId(Product productId) {
        this.productId = productId;
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
        if (!(object instanceof InventoryTransaction)) {
            return false;
        }
        InventoryTransaction other = (InventoryTransaction) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.InventoryTransaction[ id=" + id + " ]";
    }
    
}
