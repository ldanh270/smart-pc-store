package dto.supplier;

import java.util.UUID;

/**
 * Response DTO for supplier data.
 */
public class SupplierResponseDto {

    public UUID id;
    public String supplierName;
    public String contactInfo;
    public String componentTypes;
    public Integer leadTimeDays;
    public Boolean status;
}
