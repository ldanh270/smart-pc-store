package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "Suppliers")
public class Supplier {
    @Id
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "SupplierName")
    private String supplierName;

    @Nationalized
    @Column(name = "ContactInfo")
    private String contactInfo;

    @Column(name = "LeadTimeDays")
    private Integer leadTimeDays;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

}