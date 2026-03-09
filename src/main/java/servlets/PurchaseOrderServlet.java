package servlets;

import java.io.IOException;

import controllers.PurchaseController;
import dao.InventoryTransactionDao;
import dao.JPAUtil;
import dao.ProductDao;
import dao.PurchaseOrderDao;
import dao.PurchaseOrderItemDao;
import dao.SupplierDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PurchaseService;
import utils.HttpUtil;

/**
 * PurchaseOrderServlet handles purchase-order and goods-receipt HTTP requests.
 */
@WebServlet(name = "PurchaseOrderServlet", urlPatterns = {"/purchase-orders/*"})
public class PurchaseOrderServlet extends HttpServlet {

    private PurchaseController buildController() {
        PurchaseService purchaseService = new PurchaseService(
                new PurchaseOrderDao(),
                new PurchaseOrderItemDao(),
                new SupplierDao(),
                new ProductDao(),
                new InventoryTransactionDao()
        );
        return new PurchaseController(purchaseService);
    }

    /**
     * Route purchase-order GET endpoints.
     * GET /purchase-orders/ - Get all purchase orders
     * GET /purchase-orders/{id} - Get purchase order by ID
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        PurchaseController purchaseController = buildController();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                purchaseController.handleGetAllPo(req, resp);
                return;
            }
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                purchaseController.handleGetPoById(resp, parts[1]);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
            System.err.println("ERROR PurchaseOrderServlet - doGet: " + e.getMessage());
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
     * Route purchase-order POST endpoints.
     * POST /purchase-orders/create - Create new purchase order
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        PurchaseController purchaseController = buildController();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            JPAUtil.closeEntityManager();
            return;
        }

        try {
            if ("/create".equals(pathInfo)) {
                purchaseController.handleCreatePo(req, resp);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
            System.err.println("ERROR PurchaseOrderServlet - doPost: " + e.getMessage());
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
     * Route purchase-order PUT endpoints.
     * PUT - Update purchase order by ID
     * /purchase-orders/update/{id}
     * /purchase-orders/{id}
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        PurchaseController purchaseController = buildController();

        try {
            if (pathInfo != null) {
                String[] parts = pathInfo.split("/");
                // Expected format: /purchase-orders/update/{id}
                if (parts.length == 3 && "update".equals(parts[1]) && !parts[2].isBlank()) {
                    purchaseController.handleUpdatePo(req, resp, parts[2]);
                    return;
                }
                // Also support /purchase-orders/{id}
                if (parts.length == 2 && !parts[1].isBlank()) {
                    purchaseController.handleUpdatePo(req, resp, parts[1]);
                    return;
                }
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
            System.err.println("ERROR PurchaseOrderServlet - doPut: " + e.getMessage());
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
