package servlets;

import java.io.IOException;

import controllers.SupplierQuotationController;
import dao.JPAUtil;
import dao.SupplierPriceHistoryDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.SupplierQuotationService;
import utils.HttpUtil;

/**
 * SupplierQuotationServlet handles quotation history HTTP requests.
 */
@WebServlet(name = "SupplierQuotationServlet", urlPatterns = {"/supplier-quotations/*"})
public class SupplierQuotationServlet extends HttpServlet {

    private SupplierQuotationController buildController() {
        SupplierPriceHistoryDao priceHistoryDao = new SupplierPriceHistoryDao();
        SupplierQuotationService quotationService = new SupplierQuotationService(priceHistoryDao);
        return new SupplierQuotationController(quotationService);
    }

    /**
     * Route quotation GET endpoints.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        SupplierQuotationController supplierQuotationController = buildController();
        try {
            if (pathInfo == null || "/".equals(pathInfo) || "/history".equals(pathInfo)) {
                supplierQuotationController.handleGetHistory(req, resp);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
            System.err.println("ERROR SupplierQuotationServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        } finally {
            JPAUtil.closeEntityManager();
        }
    }

    /**
     * Route quotation POST endpoints.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        SupplierQuotationController supplierQuotationController = buildController();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            JPAUtil.closeEntityManager();
            return;
        }

        try {
            if ("/create".equals(pathInfo)) {
                supplierQuotationController.handleCreate(req, resp);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
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
