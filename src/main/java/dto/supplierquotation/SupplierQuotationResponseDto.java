package dto.supplierquotation;

import java.math.BigDecimal;

/**
 * Response DTO for supplier quotation history records.
 */
public class SupplierQuotationResponseDto {
    public Integer id;
    public Integer supplierId;
    public String supplierName;
    public Integer productId;
    public String productName;
    public BigDecimal importPrice;
    public String effectiveDate;
}
