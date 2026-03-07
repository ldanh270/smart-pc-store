package dto.supplieranalytics;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for latest supplier price comparison.
 */
public class PriceCompareDto {

    public UUID supplierId;
    public String supplierName;
    public UUID productId;
    public String productName;
    public BigDecimal importPrice;
    public String effectiveDate;
}
