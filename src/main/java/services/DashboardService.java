package services;

import dao.DashboardDao;
import dao.OrderDAO;
import dto.dashboard.DailyRevenuePointDto;
import dto.dashboard.DailyRevenueResponseDto;
import dto.dashboard.DashboardCategoryStatDto;
import dto.dashboard.DashboardOverviewDto;
import entities.Order;
import utils.EnvHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that computes dashboard overview statistics.
 * Compares current month vs previous month to calculate change percentages.
 */
public class DashboardService {

    private static final String OTHER_CATEGORY_NAME = "Khác";
    private static final int TOP_CATEGORY_LIMIT = 4;
    private static final int DEFAULT_DAYS = 7;
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 30;
    private static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";

    private final DashboardDao dashboardDao;
    private final OrderDAO orderDao;

    public DashboardService(DashboardDao dashboardDao, OrderDAO orderDao) {
        this.dashboardDao = dashboardDao;
        this.orderDao = orderDao;
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
     * Build category distribution data for chart: [{name, value}].
     * Returns top categories and groups the rest into "Khac".
     */
    public List<DashboardCategoryStatDto> getCategoryStats() {
        List<Object[]> rows = dashboardDao.getProductCountByCategory();

        Map<String, Long> mergedByName = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String rawName = row[0] != null ? row[0].toString() : null;
            String name = normalizeCategoryName(rawName);
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            mergedByName.merge(name, count, Long::sum);
        }

        long otherCount = mergedByName.getOrDefault(OTHER_CATEGORY_NAME, 0L);
        mergedByName.remove(OTHER_CATEGORY_NAME);

        List<Map.Entry<String, Long>> sortedCategories = new ArrayList<>(mergedByName.entrySet());
        sortedCategories.sort(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()));

        List<DashboardCategoryStatDto> result = new ArrayList<>();
        int limit = Math.min(TOP_CATEGORY_LIMIT, sortedCategories.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Long> entry = sortedCategories.get(i);
            result.add(new DashboardCategoryStatDto(entry.getKey(), entry.getValue()));
        }

        for (int i = TOP_CATEGORY_LIMIT; i < sortedCategories.size(); i++) {
            otherCount += sortedCategories.get(i).getValue();
        }

        if (otherCount > 0) {
            result.add(new DashboardCategoryStatDto(OTHER_CATEGORY_NAME, otherCount));
        }

        return result;
    }

    /**
     * Get daily revenue and order count in the last N days.
     * Missing days are returned with zero values for chart continuity.
     */
    public DailyRevenueResponseDto getDailyRevenue(Integer requestedDays) {
        int days = normalizeDays(requestedDays);
        ZoneId zoneId = resolveBusinessZone();

        LocalDate today = LocalDate.now(zoneId);
        LocalDate startDate = today.minusDays(days - 1L);
        OffsetDateTime fromInclusive = startDate.atStartOfDay(zoneId).toOffsetDateTime();

        List<Order> paidOrders = orderDao.findPaidOrdersFrom(fromInclusive);
        Map<LocalDate, BigDecimal> revenueByDate = new HashMap<>();
        Map<LocalDate, Integer> ordersByDate = new HashMap<>();

        for (Order order : paidOrders) {
            if (order.getCreatedAt() == null) {
                continue;
            }

            LocalDate revenueDate = order.getCreatedAt().atZoneSameInstant(zoneId).toLocalDate();
            if (revenueDate.isBefore(startDate) || revenueDate.isAfter(today)) {
                continue;
            }

            BigDecimal amount = toBigDecimal(order.getAmount());
            revenueByDate.merge(revenueDate, amount, BigDecimal::add);
            ordersByDate.merge(revenueDate, 1, Integer::sum);
        }

        List<DailyRevenuePointDto> items = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = 0;

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            BigDecimal revenue = revenueByDate.getOrDefault(date, BigDecimal.ZERO);
            Integer orders = ordersByDate.getOrDefault(date, 0);

            DailyRevenuePointDto point = new DailyRevenuePointDto();
            point.setDate(date.toString());
            point.setRevenue(revenue);
            point.setOrders(orders);
            items.add(point);

            totalRevenue = totalRevenue.add(revenue);
            totalOrders += orders;
        }

        DailyRevenueResponseDto response = new DailyRevenueResponseDto();
        response.setDays(days);
        response.setTimezone(zoneId.getId());
        response.setTotalRevenue(totalRevenue);
        response.setTotalOrders(totalOrders);
        response.setItems(items);
        return response;
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

    private String normalizeCategoryName(String rawName) {
        if (rawName == null) {
            return OTHER_CATEGORY_NAME;
        }
        String trimmed = rawName.trim();
        return trimmed.isEmpty() ? OTHER_CATEGORY_NAME : trimmed;
    }

    /**
     * Validate day window for revenue graph.
     */
    private int normalizeDays(Integer requestedDays) {
        if (requestedDays == null) {
            return DEFAULT_DAYS;
        }
        if (requestedDays < MIN_DAYS || requestedDays > MAX_DAYS) {
            throw new IllegalArgumentException("Days must be between 1 and 30");
        }
        return requestedDays;
    }

    /**
     * Resolve business timezone from environment with safe fallback.
     */
    private ZoneId resolveBusinessZone() {
        String timezone = EnvHelper.get("BUSINESS_TIMEZONE", DEFAULT_TIMEZONE);
        try {
            return ZoneId.of(timezone);
        } catch (RuntimeException ignored) {
            return ZoneId.of(DEFAULT_TIMEZONE);
        }
    }

    /**
     * Convert order amount into money scale used by dashboard response.
     */
    private BigDecimal toBigDecimal(Double amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
