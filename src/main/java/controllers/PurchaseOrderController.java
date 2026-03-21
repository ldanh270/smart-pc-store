package controllers;

import entities.PurchaseOrder;
import entities.PurchaseOrderItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PurchaseOrderService;
import utils.HttpUtil;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * PurchaseOrderController - 仕入注文関連のリクエストを処理するコントローラー
 */
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController() {
        this.purchaseOrderService = new PurchaseOrderService();
    }

    /**
     * 通常の仕入注文を作成する
     * POST /api/purchase-orders
     */
    public void createPurchaseOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PurchaseOrderRequestDto dto = HttpUtil.jsonToClass(req.getReader(), PurchaseOrderRequestDto.class);
            PurchaseOrder order = new PurchaseOrder();
            order.setSupplier(dto.getSupplier());
            
            PurchaseOrder savedOrder = purchaseOrderService.createPurchaseOrder(order, dto.getItems());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, savedOrder);
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 調整注文を作成する
     * POST /api/purchase-orders/{id}/adjust
     */
    public void createAdjustmentOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo(); // Expected: /{id}/adjust
            String[] parts = pathInfo.split("/");
            if (parts.length < 2) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "注文IDが必要です");
                return;
            }
            UUID parentId = UUID.fromString(parts[1]);

            AdjustmentRequestDto dto = HttpUtil.jsonToClass(req.getReader(), AdjustmentRequestDto.class);
            PurchaseOrder adjustmentOrder = purchaseOrderService.createAdjustmentOrder(parentId, dto.getItems());
            
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, adjustmentOrder);
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 調整後の数量を含むアイテム詳細を取得する
     * GET /api/purchase-orders/items/{itemId}?poId={poId}
     */
    public void getItemDetailsWithAdjustment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo(); // Expected: /items/{itemId}
            String[] parts = pathInfo.split("/");
            if (parts.length < 3) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "アイテムIDが必要です");
                return;
            }
            UUID productId = UUID.fromString(parts[2]);
            String poIdStr = req.getParameter("poId");
            if (poIdStr == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "元の注文ID(poId)が必要です");
                return;
            }
            UUID poId = UUID.fromString(poIdStr);

            int adjustedQty = purchaseOrderService.getAdjustedQuantity(poId, productId);
            
            ItemAdjustmentResponseDto response = new ItemAdjustmentResponseDto();
            response.setProductId(productId);
            response.setPoId(poId);
            response.setAdjustedQuantity(adjustedQty);

            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, response);
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // DTO classes (Inner or separate)
    public static class PurchaseOrderRequestDto {
        private entities.Supplier supplier;
        private List<PurchaseOrderItem> items;
        public entities.Supplier getSupplier() { return supplier; }
        public List<PurchaseOrderItem> getItems() { return items; }
    }

    public static class AdjustmentRequestDto {
        private List<PurchaseOrderItem> items;
        public List<PurchaseOrderItem> getItems() { return items; }
    }

    public static class ItemAdjustmentResponseDto {
        private UUID productId;
        private UUID poId;
        private int adjustedQuantity;
        public void setProductId(UUID productId) { this.productId = productId; }
        public void setPoId(UUID poId) { this.poId = poId; }
        public void setAdjustedQuantity(int adjustedQuantity) { this.adjustedQuantity = adjustedQuantity; }
    }
}
