package servlets;

import controllers.SupplierAnalyticsController;
import dao.JPAUtil;
import dao.SupplierPriceHistoryDao;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.SupplierAnalyticsService;
import utils.HttpUtil;

import java.io.IOException;

/**
 * SupplierAnalyticsServlet handles supplier-analytics HTTP requests.
 */
@WebServlet(name = "SupplierAnalyticsServlet", urlPatterns = {"/supplier-analytics/*"})
public class SupplierAnalyticsServlet extends HttpServlet {
    private SupplierAnalyticsController buildController(EntityManager em) {
        SupplierAnalyticsService supplierAnalyticsService = new SupplierAnalyticsService(new SupplierPriceHistoryDao(em));
        return new SupplierAnalyticsController(supplierAnalyticsService);
    }

    /**
     * Route analytics GET endpoints.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        SupplierAnalyticsController supplierAnalyticsController = buildController(em);
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
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}
