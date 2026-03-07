package controllers;

import dto.supplieranalytics.PriceCompareDto;
import dto.supplieranalytics.PriceTrendResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.SupplierAnalyticsService;
import utils.HttpUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Controller class for supplier analytics endpoints.
 * Exposes price comparison and price trend APIs.
 */
public class SupplierAnalyticsController {

    private final SupplierAnalyticsService supplierAnalyticsService;

    /**
     * Constructor
     *
     * @param supplierAnalyticsService Supplier analytics service dependency.
     */
    public SupplierAnalyticsController(SupplierAnalyticsService supplierAnalyticsService) {
        this.supplierAnalyticsService = supplierAnalyticsService;
    }

    /**
     * Handle GET request for latest price comparison across suppliers.
     */
    public void handlePriceCompare(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID productId = UUID.fromString(req.getParameter("productId"));
            List<PriceCompareDto> result = supplierAnalyticsService.compareLatestPrices(productId);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, result);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "productId must be a valid integer");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handle GET request for supplier price trend analysis.
     */
    public void handlePriceTrend(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID productId = UUID.fromString(req.getParameter("productId"));
            UUID supplierId = UUID.fromString(req.getParameter("supplierId"));
            PriceTrendResponseDto result = supplierAnalyticsService.getPriceTrend(productId, supplierId);
            if (result == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "No price history found");
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, result);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "productId and supplierId must be valid integers"
            );
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}
