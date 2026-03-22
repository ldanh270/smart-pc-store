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
 * PurchaseOrderServlet - 仕入注文および入庫のHTTPリクエストを処理するサーブレット
 */
@WebServlet(name = "PurchaseOrderServlet", urlPatterns = {"/api/purchase-orders/*"})
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
     * GET /api/purchase-orders/items/{itemId}?poId={poId}
     * アイテムの調整後数量を取得する
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        PurchaseController controller = buildController();
        try {
            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    controller.handleGetAdjustedQuantity(req, resp, parts[2]);
                    return;
                }
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
     * POST /api/purchase-orders (通常注文의 作成)
     * POST /api/purchase-orders/{id}/adjust (調整注文의 作成)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        PurchaseController controller = buildController();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                controller.handleCreatePo(req, resp);
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length == 3 && "adjust".equals(parts[2])) {
                controller.handleCreateAdjustmentPo(req, resp, parts[1]);
                return;
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
