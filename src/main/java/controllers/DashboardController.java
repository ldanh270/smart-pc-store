package controllers;

import dto.dashboard.DailyRevenueResponseDto;
import dto.dashboard.DashboardCategoryStatDto;
import dto.dashboard.DashboardOverviewDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.DashboardService;
import utils.HttpUtil;

import java.io.IOException;
import java.util.List;

/**
 * Controller for dashboard-related endpoints.
 */
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * GET /dashboard/overview
     * Returns overview statistics (revenue, orders, customers, products sold).
     */
    public void handleGetOverview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            DashboardOverviewDto overview = dashboardService.getOverview();
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, overview);
        } catch (Exception e) {
            System.err.println("ERROR DashboardController - handleGetOverview: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error fetching dashboard data: " + e.getMessage());
        }
    }

    /**
     * GET /dashboard/category-stats
     * Returns product distribution by category for chart usage.
     */
    public void handleGetCategoryStats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<DashboardCategoryStatDto> stats = dashboardService.getCategoryStats();
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, stats);
        } catch (Exception e) {
            System.err.println("ERROR DashboardController - handleGetCategoryStats: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error fetching category stats: " + e.getMessage());
        }
    }

    /**
     * GET /dashboard/revenue-daily
     * Returns daily revenue series for chart usage.
     */
    public void handleGetDailyRevenue(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String daysStr = req.getParameter("days");
            Integer days = (daysStr == null || daysStr.isBlank()) ? null : Integer.valueOf(daysStr);
            DailyRevenueResponseDto result = dashboardService.getDailyRevenue(days);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, result);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Days must be a valid integer");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Error: " + e.getMessage());
        }
    }
}
