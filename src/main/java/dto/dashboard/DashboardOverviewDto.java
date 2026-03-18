package dto.dashboard;

import java.math.BigDecimal;

/**
 * DTO for dashboard overview statistics.
 */
public class DashboardOverviewDto {

    /** Tổng doanh thu (total revenue) */
    private BigDecimal totalRevenue;

    /** % thay đổi doanh thu so với tháng trước */
    private Double revenueChangePercent;

    /** Đơn hàng mới (new orders count) */
    private Long newOrders;

    /** % thay đổi đơn hàng so với tháng trước */
    private Double ordersChangePercent;

    /** Khách hàng mới (new customers count) */
    private Long newCustomers;

    /** % thay đổi khách hàng so với tháng trước */
    private Double customersChangePercent;

    /** Sản phẩm đã bán (products sold count) */
    private Long productsSold;

    /** % thay đổi sản phẩm đã bán so với tháng trước */
    private Double productsSoldChangePercent;

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getRevenueChangePercent() {
        return revenueChangePercent;
    }

    public void setRevenueChangePercent(Double revenueChangePercent) {
        this.revenueChangePercent = revenueChangePercent;
    }

    public Long getNewOrders() {
        return newOrders;
    }

    public void setNewOrders(Long newOrders) {
        this.newOrders = newOrders;
    }

    public Double getOrdersChangePercent() {
        return ordersChangePercent;
    }

    public void setOrdersChangePercent(Double ordersChangePercent) {
        this.ordersChangePercent = ordersChangePercent;
    }

    public Long getNewCustomers() {
        return newCustomers;
    }

    public void setNewCustomers(Long newCustomers) {
        this.newCustomers = newCustomers;
    }

    public Double getCustomersChangePercent() {
        return customersChangePercent;
    }

    public void setCustomersChangePercent(Double customersChangePercent) {
        this.customersChangePercent = customersChangePercent;
    }

    public Long getProductsSold() {
        return productsSold;
    }

    public void setProductsSold(Long productsSold) {
        this.productsSold = productsSold;
    }

    public Double getProductsSoldChangePercent() {
        return productsSoldChangePercent;
    }

    public void setProductsSoldChangePercent(Double productsSoldChangePercent) {
        this.productsSoldChangePercent = productsSoldChangePercent;
    }
}
