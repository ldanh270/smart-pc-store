package dto.supplierquotation;

import java.math.BigDecimal;

/**
 * Request DTO for creating a supplier quotation record.
 */
public class SupplierQuotationRequestDto {
    public Integer supplierId;
    public Integer productId;
    public BigDecimal importPrice;
    public String effectiveDate;
}
