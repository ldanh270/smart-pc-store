package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "Suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "SupplierName")
    private String supplierName;

    @Nationalized
    @Column(name = "ContactInfo")
    private String contactInfo;

    @Nationalized
    @Column(name = "ComponentTypes")
    private String componentTypes;

    @Column(name = "LeadTimeDays")
    private Integer leadTimeDays;

    @Column(name = "Status", nullable = false)
    private Boolean status = true;

    /**
     * Ensure default active status for new records.
     */
    @PrePersist
    public void prePersist() {
        if (status == null) status = true;
    }

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

    public String getComponentTypes() {
        return componentTypes;
    }

    public void setComponentTypes(String componentTypes) {
        this.componentTypes = componentTypes;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

}
