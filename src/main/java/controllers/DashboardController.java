package controllers;

import dto.dashboard.DashboardCategoryStatDto;
import dto.dashboard.DashboardOverviewDto;
import dto.dashboard.DashboardTopProductDto;
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
     * GET /dashboard/top-products
     * Returns top 5 best-selling products.
     */
    public void handleGetTopProducts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<DashboardTopProductDto> topProducts = dashboardService.getTopProducts(5);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, topProducts);
        } catch (Exception e) {
            System.err.println("ERROR DashboardController - handleGetTopProducts: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error fetching top products: " + e.getMessage());
        }
    }
}
