package dto.supplier;

/**
 * Request DTO for creating/updating supplier records.
 */
public class SupplierRequestDto {

    public String supplierName;
    public String contactInfo;
    public String componentTypes;
    public Integer leadTimeDays;
    public Boolean status;
}
