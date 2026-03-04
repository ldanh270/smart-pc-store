package dto.supplieranalytics;

import java.math.BigDecimal;

/**
 * Response DTO for latest supplier price comparison.
 */
public class PriceCompareDto {

    public Integer supplierId;
    public String supplierName;
    public Integer productId;
    public String productName;
    public BigDecimal importPrice;
    public String effectiveDate;
}
