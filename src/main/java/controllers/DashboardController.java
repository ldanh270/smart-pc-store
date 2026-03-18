package controllers;

import dto.dashboard.DashboardOverviewDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.DashboardService;
import utils.HttpUtil;

import java.io.IOException;

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
}
