package dto.supplierquotation;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a supplier quotation record.
 */
public class SupplierQuotationRequestDto {

    public UUID supplierId;
    public UUID productId;
    public BigDecimal importPrice;
    public String effectiveDate;
}
