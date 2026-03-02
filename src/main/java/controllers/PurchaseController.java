package controllers;

import dto.ApiResponse;
import dto.purchase.GoodsReceiptRequestDto;
import dto.purchase.GoodsReceiptResponseDto;
import dto.purchase.PurchaseOrderCreateRequestDto;
import dto.purchase.PurchaseOrderResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PurchaseService;
import utils.HttpUtil;

import java.io.IOException;

/**
 * Controller class for purchasing endpoints (PO and GRN).
 * Handles purchase order creation, detail retrieval, and goods receipt actions.
 */
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * Constructor.
     *
     * @param purchaseService Purchase service dependency.
     */
    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * Handle POST request for creating a purchase order.
     */
    public void handleCreatePo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PurchaseOrderCreateRequestDto dto = HttpUtil.jsonToClass(req.getReader(), PurchaseOrderCreateRequestDto.class);
            PurchaseOrderResponseDto created = purchaseService.createPurchaseOrder(dto);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, new ApiResponse<>(true, "PO created", created));
        } catch (IllegalArgumentException e) {
            int status = ("Supplier not found".equals(e.getMessage()) || e.getMessage().startsWith("Product not found:"))
                    ? HttpServletResponse.SC_NOT_FOUND
                    : HttpServletResponse.SC_BAD_REQUEST;
            HttpUtil.sendJson(resp, status, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiResponse<>(false, "Internal server error", null));
        }
    }

    /**
     * Handle GET request for retrieving a purchase order by id.
     */
    public void handleGetPoById(HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            PurchaseOrderResponseDto dto = purchaseService.getPurchaseOrder(id);
            if (dto == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Purchase order not found");
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, dto);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid purchase order id");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handle POST request for receiving goods and creating a GRN for a PO.
     */
    public void handleReceiveGoods(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer poId = Integer.parseInt(idStr);
            GoodsReceiptRequestDto dto = HttpUtil.jsonToClass(req.getReader(), GoodsReceiptRequestDto.class);
            GoodsReceiptResponseDto received = purchaseService.receiveGoods(poId, dto);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, new ApiResponse<>(true, "Goods received", received));
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiResponse<>(false, "Invalid purchase order id", null));
        } catch (IllegalArgumentException e) {
            int status = "Purchase order not found".equals(e.getMessage())
                    ? HttpServletResponse.SC_NOT_FOUND
                    : HttpServletResponse.SC_BAD_REQUEST;
            HttpUtil.sendJson(resp, status, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiResponse<>(false, "Internal server error", null));
        }
    }
}
