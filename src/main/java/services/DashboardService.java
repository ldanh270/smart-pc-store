package services;

import dao.DashboardDao;
import dto.dashboard.DashboardOverviewDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Service that computes dashboard overview statistics.
 * Compares current month vs previous month to calculate change percentages.
 */
public class DashboardService {

    private final DashboardDao dashboardDao;

    public DashboardService(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    /**
     * Get overview statistics for the dashboard.
     *
     * @return DashboardOverviewDto with current-month stats and month-over-month change %
     */
    public DashboardOverviewDto getOverview() {
        // Current month boundaries
        LocalDate today = LocalDate.now();
        LocalDate firstDayCurrentMonth = today.withDayOfMonth(1);
        LocalDate firstDayNextMonth = firstDayCurrentMonth.plusMonths(1);
        LocalDate firstDayPrevMonth = firstDayCurrentMonth.minusMonths(1);

        OffsetDateTime currentFrom = firstDayCurrentMonth.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime currentTo = firstDayNextMonth.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime prevFrom = firstDayPrevMonth.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime prevTo = currentFrom; // end of prev month = start of current month

        // ── Current month stats ──
        BigDecimal currentRevenue = dashboardDao.getTotalRevenue(currentFrom, currentTo);
        Long currentOrders = dashboardDao.countOrders(currentFrom, currentTo);
        Long currentCustomers = dashboardDao.countNewCustomers(currentFrom, currentTo);
        Long currentProductsSold = dashboardDao.countProductsSold(currentFrom, currentTo);

        // ── Previous month stats ──
        BigDecimal prevRevenue = dashboardDao.getTotalRevenue(prevFrom, prevTo);
        Long prevOrders = dashboardDao.countOrders(prevFrom, prevTo);
        Long prevCustomers = dashboardDao.countNewCustomers(prevFrom, prevTo);
        Long prevProductsSold = dashboardDao.countProductsSold(prevFrom, prevTo);

        // ── Build response ──
        DashboardOverviewDto dto = new DashboardOverviewDto();
        dto.setTotalRevenue(currentRevenue);
        dto.setRevenueChangePercent(calcChangePercent(currentRevenue.doubleValue(), prevRevenue.doubleValue()));

        dto.setNewOrders(currentOrders);
        dto.setOrdersChangePercent(calcChangePercent(currentOrders.doubleValue(), prevOrders.doubleValue()));

        dto.setNewCustomers(currentCustomers);
        dto.setCustomersChangePercent(calcChangePercent(currentCustomers.doubleValue(), prevCustomers.doubleValue()));

        dto.setProductsSold(currentProductsSold);
        dto.setProductsSoldChangePercent(calcChangePercent(currentProductsSold.doubleValue(), prevProductsSold.doubleValue()));

        return dto;
    }

    /**
     * Calculate percentage change: ((current - previous) / previous) * 100
     * Returns 0.0 if previous is 0 to avoid division by zero.
     */
    private Double calcChangePercent(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        double change = ((current - previous) / previous) * 100.0;
        return BigDecimal.valueOf(change).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
