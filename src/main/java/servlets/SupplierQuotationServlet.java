package servlets;

import controllers.SupplierQuotationController;
import dao.JPAUtil;
import dao.SupplierPriceHistoryDao;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.SupplierQuotationService;
import utils.HttpUtil;

import java.io.IOException;

/**
 * SupplierQuotationServlet handles quotation history HTTP requests.
 */
@WebServlet(name = "SupplierQuotationServlet", urlPatterns = {"/supplier-quotations/*"})
public class SupplierQuotationServlet extends HttpServlet {
    private SupplierQuotationController buildController(EntityManager em) {
        SupplierPriceHistoryDao priceHistoryDao = new SupplierPriceHistoryDao(em);
        SupplierQuotationService quotationService = new SupplierQuotationService(priceHistoryDao, em);
        return new SupplierQuotationController(quotationService);
    }

    /**
     * Route quotation GET endpoints.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        SupplierQuotationController supplierQuotationController = buildController(em);
        try {
            if (pathInfo == null || "/".equals(pathInfo) || "/history".equals(pathInfo)) {
                supplierQuotationController.handleGetHistory(req, resp);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    /**
     * Route quotation POST endpoints.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        SupplierQuotationController supplierQuotationController = buildController(em);
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            if (em.isOpen()) em.close();
            return;
        }

        try {
            if ("/create".equals(pathInfo)) {
                supplierQuotationController.handleCreate(req, resp);
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
