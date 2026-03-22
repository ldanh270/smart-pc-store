package dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO response for daily revenue dashboard endpoint.
 */
public class DailyRevenueResponseDto {

    /** Requested day window */
    private Integer days;

    /** Timezone used to aggregate the daily buckets */
    private String timezone;

    /** Total revenue in the returned period */
    private BigDecimal totalRevenue;

    /** Total paid orders in the returned period */
    private Integer totalOrders;

    /** Daily points used for graph plotting */
    private List<DailyRevenuePointDto> items;

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public List<DailyRevenuePointDto> getItems() {
        return items;
    }

    public void setItems(List<DailyRevenuePointDto> items) {
        this.items = items;
    }
}
