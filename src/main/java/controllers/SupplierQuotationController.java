package controllers;

import java.io.IOException;
import java.util.List;

import dto.ApiResponse;
import dto.supplierquotation.SupplierQuotationRequestDto;
import dto.supplierquotation.SupplierQuotationResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.SupplierQuotationService;
import utils.HttpUtil;

/**
 * Controller class for supplier quotation endpoints. Handles quotation creation
 * and history retrieval requests.
 */
public class SupplierQuotationController {

    private final SupplierQuotationService supplierQuotationService;

    /**
     * Constructor
     *
     * @param supplierQuotationService Supplier quotation service dependency.
     */
    public SupplierQuotationController(SupplierQuotationService supplierQuotationService) {
        this.supplierQuotationService = supplierQuotationService;
    }

    /**
     * Handle POST request for creating quotation history record.
     */
    public void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            SupplierQuotationRequestDto dto = HttpUtil.jsonToClass(req.getReader(), SupplierQuotationRequestDto.class);
            SupplierQuotationResponseDto created = supplierQuotationService.create(dto);
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_CREATED,
                    new ApiResponse<>(true, "Quotation created", created)
            );
        } catch (IllegalArgumentException e) {
            int status = ("Supplier not found".equals(e.getMessage()) || "Product not found".equals(e.getMessage())) ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            HttpUtil.sendJson(resp, status, new ApiResponse<>(false, e.getMessage(), null));
        } catch (IOException e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(false, "Internal server error", null)
            );
        }
    }

    /**
     * Handle GET request for quotation history by product and supplier.
     */
    public void handleGetHistory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer productId = Integer.valueOf(req.getParameter("productId"));
            Integer supplierId = Integer.valueOf(req.getParameter("supplierId"));
            List<SupplierQuotationResponseDto> history = supplierQuotationService.getHistory(productId, supplierId);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, history);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "productId and supplierId must be valid integers"
            );
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}
