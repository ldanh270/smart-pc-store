package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Table(name = "\"Suppliers\"")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "\"supplierName\"", length = Integer.MAX_VALUE)
    private String supplierName;

    @Column(name = "\"contactInfo\"", length = Integer.MAX_VALUE)
    private String contactInfo;

    @Column(name = "\"leadTimeDays\"")
    private Integer leadTimeDays;

    @Column(name = "\"componentTypes\"", length = Integer.MAX_VALUE)
    private String componentTypes;

    @ColumnDefault("true")
    @Column(name = "status", nullable = false)
    private Boolean status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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
