package controllers;

import dto.ApiResponse;
import dto.purchase.PurchaseOrderCreateRequestDto;
import dto.purchase.PurchaseOrderResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PurchaseService;
import utils.HttpUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Controller class for purchasing endpoints.
 * Handles purchase order creation and detail retrieval.
 */
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * Constructor
     *
     * @param purchaseService Purchase service dependency.
     */
    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * Handle GET request for retrieving all purchase orders.
     */
    public void handleGetAllPo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String q = req.getParameter("q");
            if (q == null || q.isBlank()) {
                q = req.getParameter("query");
            }
            String pageStr = req.getParameter("page");
            String sizeStr = req.getParameter("size");

            Integer page = (pageStr == null || pageStr.isBlank()) ? null : Integer.valueOf(pageStr);
            Integer size = (sizeStr == null || sizeStr.isBlank()) ? null : Integer.valueOf(sizeStr);

            if (page == null && size != null) {
                page = 1;
            }
            if (page != null && size == null) {
                size = 5;
            }
            if (page != null && page < 0) {
                throw new IllegalArgumentException("page must be >= 0");
            }
            if (size != null && size <= 0) {
                throw new IllegalArgumentException("size must be > 0");
            }

            List<PurchaseOrderResponseDto> dto = purchaseService.getAllPurchaseOrders(q, page, size);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, dto);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric query parameter");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handle GET request for retrieving a purchase order by id.
     */
    public void handleGetPoById(HttpServletResponse resp, String idStr) throws IOException {
        try {
            UUID id = UUID.fromString(idStr);
            PurchaseOrderResponseDto dto = purchaseService.getPurchaseOrder(id);
            if (dto == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Purchase order not found");
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, dto);
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid purchase order id");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
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
            int status = ("Supplier not found".equals(e.getMessage()) || e.getMessage().startsWith("Product not found:")) ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            HttpUtil.sendJson(resp, status, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiResponse<>(false, "Internal server error", null));
        }
    }

    /**
     * Handle PUT request for updating a purchase order.
     */
    public void handleUpdatePo(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            UUID id = UUID.fromString(idStr);
            PurchaseOrderCreateRequestDto dto = HttpUtil.jsonToClass(req.getReader(), PurchaseOrderCreateRequestDto.class);
            PurchaseOrderResponseDto updated = purchaseService.updatePurchaseOrder(id, dto);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, new ApiResponse<>(true, "PO updated", updated));
        } catch (IllegalArgumentException e) {
            int status = ("Supplier not found".equals(e.getMessage()) || e.getMessage().startsWith("Product not found:") || "Purchase order not found".equals(e.getMessage())) ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            HttpUtil.sendJson(resp, status, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiResponse<>(false, "Internal server error", null));
        }
    }
    /**
     * Handle POST request for creating an adjustment order.
     */
    public void handleCreateAdjustmentPo(HttpServletRequest req, HttpServletResponse resp, String parentIdStr) throws IOException {
        try {
            UUID parentId = UUID.fromString(parentIdStr);
            PurchaseOrderCreateRequestDto dto = HttpUtil.jsonToClass(req.getReader(), PurchaseOrderCreateRequestDto.class);
            PurchaseOrderResponseDto created = purchaseService.createAdjustmentOrder(parentId, dto);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, new ApiResponse<>(true, "Adjustment PO created", created));
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiResponse<>(false, e.getMessage(), null));
        } catch (IllegalStateException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CONFLICT, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiResponse<>(false, "Internal server error", null));
        }
    }

    /**
     * Handle GET request for retrieving adjusted quantity of an item.
     */
    public void handleGetAdjustedQuantity(HttpServletRequest req, HttpServletResponse resp, String productIdStr) throws IOException {
        try {
            UUID productId = UUID.fromString(productIdStr);
            String poIdStr = req.getParameter("poId");
            if (poIdStr == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "poId is required");
                return;
            }
            UUID poId = UUID.fromString(poIdStr);
            int adjustedQuantity = purchaseService.getAdjustedQuantity(poId, productId);
            
            // Re-using a simple map for response
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("productId", productId);
            result.put("poId", poId);
            result.put("adjustedQuantity", adjustedQuantity);
            
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, result);
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}
