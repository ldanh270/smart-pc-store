package servlets;

import controllers.PurchaseController;
import dao.*;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PurchaseService;
import utils.HttpUtil;

import java.io.IOException;

/**
 * PurchaseOrderServlet handles purchase-order and goods-receipt HTTP requests.
 */
@WebServlet(name = "PurchaseOrderServlet", urlPatterns = {"/purchase-orders/*"})
public class PurchaseOrderServlet extends HttpServlet {
    private PurchaseController buildController(EntityManager em) {
        PurchaseService purchaseService = new PurchaseService(
                new PurchaseOrderDao(em),
                new PurchaseOrderItemDao(em),
                new SupplierDao(em),
                new ProductDao(em),
                new SupplierPriceHistoryDao(em),
                new GoodsReceiptNoteDao(em),
                new GoodsReceiptNoteItemDao(em),
                new InventoryTransactionDao(em),
                em
        );
        return new PurchaseController(purchaseService);
    }

    /**
     * Route purchase-order GET endpoints.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        PurchaseController purchaseController = buildController(em);
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "PO id is required");
                return;
            }
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                purchaseController.handleGetPoById(resp, parts[1]);
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
     * Route purchase-order POST endpoints.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        PurchaseController purchaseController = buildController(em);
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            if (em.isOpen()) em.close();
            return;
        }

        try {
            if ("/create".equals(pathInfo)) {
                purchaseController.handleCreatePo(req, resp);
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length == 3 && !parts[1].isBlank() && "receive".equals(parts[2])) {
                purchaseController.handleReceiveGoods(req, resp, parts[1]);
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
