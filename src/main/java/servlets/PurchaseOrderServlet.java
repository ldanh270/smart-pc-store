package servlets;

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

import java.io.IOException;

/**
 * PurchaseOrderServlet - Servlet for handling HTTP requests for purchase orders and inventory receipt
 * PurchaseOrderServlet - 仕入注文および入庫のHTTPリクエストを処理するサーブレット
 */
@WebServlet(name = "PurchaseOrderServlet", urlPatterns = {"/purchase-orders/*"})
public class PurchaseOrderServlet extends HttpServlet {

    private PurchaseController buildController() {
        PurchaseOrderDao poDao = new PurchaseOrderDao();
        PurchaseOrderItemDao poiDao = new PurchaseOrderItemDao();
        SupplierDao supplierDao = new SupplierDao();
        ProductDao productDao = new ProductDao();
        InventoryTransactionDao itDao = new InventoryTransactionDao();
        PurchaseService purchaseService = new PurchaseService(poDao, poiDao, supplierDao, productDao, itDao);
        return new PurchaseController(purchaseService);
    }

    /**
     * GET /purchase-orders
     * GET /purchase-orders/{id}
     * GET /purchase-orders/items/{itemId}?poId={poId}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        PurchaseController controller = buildController();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                controller.handleGetAllPo(req, resp);
                return;
            }

            if (pathInfo.startsWith("/items/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    controller.handleGetAdjustedQuantity(req, resp, parts[2]);
                    return;
                }
            }

            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                controller.handleGetPoById(resp, parts[1]);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            System.err.println("ERROR PurchaseOrderServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }

    /**
     * POST /purchase-orders/create
     * POST /purchase-orders/update
     * POST /purchase-orders/{id}/adjust
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        PurchaseController controller = buildController();
        try {
            if ("/create".equals(pathInfo)) {
                controller.handleCreatePo(req, resp);
                return;
            }

            if ("/update".equals(pathInfo)) {
                String idStr = req.getParameter("id");
                if (idStr != null) {
                    controller.handleUpdatePo(req, resp, idStr);
                } else {
                    HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing id parameter for update");
                }
                return;
            }

            if (pathInfo != null) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 3 && "adjust".equals(parts[2])) {
                    controller.handleCreateAdjustmentPo(req, resp, parts[1]);
                    return;
                }
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            System.err.println("ERROR PurchaseOrderServlet - doPost: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }
}
