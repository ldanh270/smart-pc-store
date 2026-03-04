package servlets;

import java.io.IOException;

import controllers.SupplierAnalyticsController;
import dao.JPAUtil;
import dao.SupplierPriceHistoryDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.SupplierAnalyticsService;
import utils.HttpUtil;

/**
 * SupplierAnalyticsServlet handles supplier-analytics HTTP requests.
 */
@WebServlet(name = "SupplierAnalyticsServlet", urlPatterns = {"/supplier-analytics/*"})
public class SupplierAnalyticsServlet extends HttpServlet {

    private SupplierAnalyticsController buildController() {
        SupplierAnalyticsService supplierAnalyticsService = new SupplierAnalyticsService(new SupplierPriceHistoryDao());
        return new SupplierAnalyticsController(supplierAnalyticsService);
    }

    /**
     * Route analytics GET endpoints.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        SupplierAnalyticsController supplierAnalyticsController = buildController();
        try {
            if ("/price-compare".equals(pathInfo)) {
                supplierAnalyticsController.handlePriceCompare(req, resp);
                return;
            }
            if ("/price-trend".equals(pathInfo)) {
                supplierAnalyticsController.handlePriceTrend(req, resp);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
            System.err.println("ERROR SupplierAnalyticsServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        } finally {
            JPAUtil.closeEntityManager();
        }
    }
}
