package dto.dashboard;

import java.math.BigDecimal;

/**
 * DTO item for daily revenue chart.
 */
public class DailyRevenuePointDto {

    /** Date in yyyy-MM-dd format */
    private String date;

    /** Revenue amount of the day */
    private BigDecimal revenue;

    /** Number of paid orders of the day */
    private Integer orders;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Integer getOrders() {
        return orders;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }
}
