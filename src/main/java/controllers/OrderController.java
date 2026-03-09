package controllers;

import dto.order.CreateOrderRequestDto;
import dto.order.OrderResponseDto;
import dto.order.OrderViewResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.OrderService;
import utils.HttpUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                page = 0;
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

            List<OrderResponseDto> orders = orderService.getAllOrders(q, page, size);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, orders);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric query parameter");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void handleGetMyOrders(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID userId = (UUID) req.getAttribute("userId");
            if (userId == null) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
                return;
            }

            String pageStr = req.getParameter("page");
            String sizeStr = req.getParameter("size");

            Integer page = (pageStr == null || pageStr.isBlank()) ? null : Integer.valueOf(pageStr);
            Integer size = (sizeStr == null || sizeStr.isBlank()) ? null : Integer.valueOf(sizeStr);

            if (page == null && size != null) {
                page = 0;
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

            List<OrderResponseDto> orders = orderService.getUserOrders(userId, page, size);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, orders);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric query parameter");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    public void handleGetDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        String action = req.getParameter("action");

        if ((id == null || id.isEmpty()) && "view".equals(action)) {
            id = req.getParameter("id"); // redundant but clear
        }

        if (id == null || id.isEmpty()) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Order ID or Code is required");
            return;
        }
        OrderViewResponseDto detail = orderService.getOrderDetails(id);
        if (detail == null) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Order not found");
        } else {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, detail);
        }
    }

    public void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateOrderRequestDto dto = HttpUtil.jsonToClass(req.getReader(), CreateOrderRequestDto.class);
            OrderResponseDto response = orderService.createOrder(dto);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, response);
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Error: " + e.getMessage());
        }
    }

    public void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "ID or OrderCode is required");
            return;
        }
        try {
            orderService.deleteOrder(idStr);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Order deleted successfully");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot delete order: " + e.getMessage());
        }
    }

    public void handleCancelOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "ID or OrderCode is required");
            return;
        }

        UUID userId = (UUID) req.getAttribute("userId");
        if (userId == null) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
            return;
        }

        try {
            orderService.cancelOrder(idStr, userId);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Order cancelled successfully");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot cancel order: " + e.getMessage());
        }
    }
}
