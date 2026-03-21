package servlets;

import controllers.DashboardController;
import dao.DashboardDao;
import dao.OrderDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.DashboardService;
import utils.HttpUtil;

import java.io.IOException;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard/*"})
public class DashboardServlet extends HttpServlet {

    private DashboardController dashboardController;

    @Override
    public void init() {
        DashboardService dashboardService = new DashboardService(new DashboardDao(), new OrderDAO());
        this.dashboardController = new DashboardController(dashboardService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Dashboard API");
                return;
            }

            switch (pathInfo) {
                case "/overview" -> dashboardController.handleGetOverview(req, resp);
                case "/category-stats" -> dashboardController.handleGetCategoryStats(req, resp);
                case "/revenue-daily" -> dashboardController.handleGetDailyRevenue(req, resp);
                default -> HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (IOException e) {
            System.err.println("ERROR DashboardServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error processing request: " + e.getMessage());
        }
    }
}
