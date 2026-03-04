package dto.supplieranalytics;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for supplier price trend analysis.
 */
public class PriceTrendResponseDto {

    public Integer supplierId;
    public String supplierName;
    public Integer productId;
    public String productName;
    public BigDecimal firstPrice;
    public BigDecimal lastPrice;
    public BigDecimal percentageChange;
    public String trend;
    public List<Point> points;

    /**
     * Time-series data point.
     */
    public static class Point {

        public String effectiveDate;
        public BigDecimal importPrice;
    }
}
