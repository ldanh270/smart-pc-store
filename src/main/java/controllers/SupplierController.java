package controllers;

import dto.ApiResponse;
import dto.supplier.SupplierRequestDto;
import dto.supplier.SupplierResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.SupplierService;
import utils.HttpUtil;

import java.io.IOException;
import java.util.List;

/**
 * Controller class for supplier management endpoints.
 * Handles supplier CRUD request/response mapping.
 */
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * Constructor.
     *
     * @param supplierService Supplier service dependency.
     */
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * Handle GET request for listing suppliers with optional search.
     */
    public void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String q = req.getParameter("q");
        List<SupplierResponseDto> result = supplierService.getAllDtos(q);
        HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, result);
    }

    /**
     * Handle GET request for supplier detail by id.
     */
    public void handleGetById(HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            SupplierResponseDto dto = supplierService.getByIdDto(id);
            if (dto == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Supplier not found");
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, dto);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid supplier id");
        }
    }

    /**
     * Handle POST request for creating a supplier.
     */
    public void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            SupplierRequestDto dto = HttpUtil.jsonToClass(req.getReader(), SupplierRequestDto.class);
            SupplierResponseDto created = supplierService.create(dto);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, new ApiResponse<>(true, "Supplier created", created));
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiResponse<>(false, "Internal server error", null));
        }
    }

    /**
     * Handle PUT request for updating supplier information.
     */
    public void handleUpdate(HttpServletRequest req, HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            SupplierRequestDto dto = HttpUtil.jsonToClass(req.getReader(), SupplierRequestDto.class);
            SupplierResponseDto updated = supplierService.update(id, dto);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, new ApiResponse<>(true, "Supplier updated", updated));
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiResponse<>(false, "Invalid supplier id", null));
        } catch (IllegalArgumentException e) {
            int status = "Supplier not found".equals(e.getMessage()) ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            HttpUtil.sendJson(resp, status, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiResponse<>(false, "Internal server error", null));
        }
    }

    /**
     * Handle DELETE request for soft-deleting a supplier.
     */
    public void handleDelete(HttpServletResponse resp, String idStr) throws IOException {
        try {
            Integer id = Integer.parseInt(idStr);
            supplierService.delete(id);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, new ApiResponse<>(true, "Supplier deactivated", null));
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiResponse<>(false, "Invalid supplier id", null));
        } catch (IllegalArgumentException e) {
            int status = "Supplier not found".equals(e.getMessage()) ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            HttpUtil.sendJson(resp, status, new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiResponse<>(false, "Internal server error", null));
        }
    }
}
