package dto.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object for top-selling products on the dashboard.
 */
public class DashboardTopProductDto {

    public UUID id;
    public String productName;
    public String slug;
    public String imageUrl;
    public BigDecimal currentPrice;
    public Long totalSold;

    public DashboardTopProductDto(UUID id, String productName, String slug, String imageUrl, BigDecimal currentPrice, Long totalSold) {
        this.id = id;
        this.productName = productName;
        this.slug = slug;
        this.imageUrl = imageUrl;
        this.currentPrice = currentPrice;
        this.totalSold = totalSold;
    }
}
