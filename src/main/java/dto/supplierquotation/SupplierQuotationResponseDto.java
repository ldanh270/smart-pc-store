package dto.supplierquotation;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for supplier quotation history records.
 */
public class SupplierQuotationResponseDto {

    public UUID id;
    public UUID supplierId;
    public String supplierName;
    public UUID productId;
    public String productName;
    public BigDecimal importPrice;
    public String effectiveDate;
}
