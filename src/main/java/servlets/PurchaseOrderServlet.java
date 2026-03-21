package servlets;

import java.io.IOException;

import controllers.PurchaseController;
import controllers.PurchaseOrderController;
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
 * PurchaseOrderServlet - 仕入注文および入庫のHTTPリクエストを処理するサーブレット
 */
@WebServlet(name = "PurchaseOrderServlet", urlPatterns = {"/api/purchase-orders/*"})
public class PurchaseOrderServlet extends HttpServlet {

    private PurchaseOrderController buildController() {
        return new PurchaseOrderController();
    }

    /**
     * GET /api/purchase-orders/items/{itemId}?poId={poId}
     * アイテムの調整後数量を取得する
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        PurchaseOrderController controller = buildController();
        try {
            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                controller.getItemDetailsWithAdjustment(req, resp);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }

    /**
     * POST /api/purchase-orders (通常注文の作成)
     * POST /api/purchase-orders/{id}/adjust (調整注文の作成)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        PurchaseOrderController controller = buildController();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                controller.createPurchaseOrder(req, resp);
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length == 3 && "adjust".equals(parts[2])) {
                controller.createAdjustmentOrder(req, resp);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }
}
