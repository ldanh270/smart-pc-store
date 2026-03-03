package servlets;

import dao.OrderDAO;
import dao.ProductDao;
import entities.OrderDetailModel;
import entities.OrderModel;
import entities.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            if (action == null) action = "list";

            switch (action) {
                case "new":
                    showNewForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteOrder(request, response);
                    break;
                case "view":
                    viewOrderDetails(request, response);
                    break;
                case "all":
                    listAllOrders(request, response);
                    break;
                default:
                    listOrders(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error processing request: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            if ("insert".equals(action)) {
                insertOrder(request, response);
            } else if ("update".equals(action)) {
                updateOrder(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error saving data: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void listOrders(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        int page = 1;
        int pageSize = 5;
        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }
        String searchCode = request.getParameter("searchCode");

        List<OrderModel> list = orderDAO.getOrders(page, pageSize, searchCode);
        int totalOrders = orderDAO.getTotalOrders(searchCode);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        if (isApiRequest(request)) {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("orderList", list);
            data.put("currentPage", page);
            data.put("totalPages", totalPages);
            data.put("totalOrders", totalOrders);
            utils.HttpUtil.sendJson(response, HttpServletResponse.SC_OK, data);
        } else {
            request.setAttribute("orderList", list);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("searchCode", searchCode);
            request.getRequestDispatcher("/orderList.jsp").forward(request, response);
        }
    }

    private void listAllOrders(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        List<OrderModel> list = orderDAO.getAllOrders();
        if (isApiRequest(request)) {
            utils.HttpUtil.sendJson(response, HttpServletResponse.SC_OK, list);
        } else {
            request.setAttribute("orderList", list);
            // Với action=all, ta hiển thị hết trên 1 trang (totalPages=1)
            request.setAttribute("currentPage", 1);
            request.setAttribute("totalPages", 1);
            request.getRequestDispatcher("/orderList.jsp").forward(request, response);
        }
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Product> products = productDao.findAll();
        request.setAttribute("products", products);
        request.getRequestDispatcher("/orderForm.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        OrderModel existingOrder = orderDAO.getOrderById(id);
        request.setAttribute("order", existingOrder);
        request.getRequestDispatcher("/orderForm.jsp").forward(request, response);
    }

    private void insertOrder(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        java.util.List<OrderDetailModel> items = new java.util.ArrayList<>();
        double totalAmount = 0;

        if (isApiRequest(request)) {
            try {
                com.google.gson.JsonObject body = com.google.gson.JsonParser.parseReader(request.getReader()).getAsJsonObject();
                com.google.gson.JsonArray jsonItems = body.getAsJsonArray("items");
                
                if (jsonItems == null || jsonItems.size() == 0) {
                    utils.HttpUtil.sendJson(response, HttpServletResponse.SC_BAD_REQUEST, "Items are required");
                    return;
                }

                for (com.google.gson.JsonElement el : jsonItems) {
                    com.google.gson.JsonObject itemObj = el.getAsJsonObject();
                    int pid = itemObj.get("productId").getAsInt();
                    int qty = itemObj.get("quantity").getAsInt();
                    
                    Product p = productDao.findById(pid);
                    if (p != null && qty > 0) {
                        double unitPrice = p.getCurrentPrice().doubleValue();
                        totalAmount += unitPrice * qty;
                        
                        OrderDetailModel item = new OrderDetailModel();
                        item.setProductId(pid);
                        item.setQuantity(qty);
                        item.setUnitPrice(unitPrice);
                        items.add(item);
                    }
                }
            } catch (Exception e) {
                utils.HttpUtil.sendJson(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + e.getMessage());
                return;
            }
        } else {
            String[] productIds = request.getParameterValues("productIds");
            if (productIds == null || productIds.length == 0) {
                throw new IllegalArgumentException("At least one product must be selected");
            }

            for (String pidStr : productIds) {
                int pid = Integer.parseInt(pidStr);
                String qtyStr = request.getParameter("qty_" + pid);
                int qty = (qtyStr == null || qtyStr.isEmpty()) ? 0 : Integer.parseInt(qtyStr);

                if (qty > 0) {
                    Product p = productDao.findById(pid);
                    if (p != null) {
                        double unitPrice = p.getCurrentPrice().doubleValue();
                        totalAmount += unitPrice * qty;

                        OrderDetailModel item = new OrderDetailModel();
                        item.setProductId(pid);
                        item.setQuantity(qty);
                        item.setUnitPrice(unitPrice);
                        items.add(item);
                    }
                }
            }
        }

        if (items.isEmpty()) {
            if (isApiRequest(request)) {
                utils.HttpUtil.sendJson(response, HttpServletResponse.SC_BAD_REQUEST, "Valid products and quantities are required");
                return;
            } else {
                throw new IllegalArgumentException("Quantity must be greater than 0 for selected products");
            }
        }

        String orderCode = "DH" + System.currentTimeMillis() % 1000000;
        String transactionCode = generateRandomString(8);
        
        OrderModel newOrder = new OrderModel();
        newOrder.setOrderCode(orderCode);
        newOrder.setAmount(totalAmount);
        newOrder.setTransactionCode(transactionCode);
        newOrder.setStatus("PENDING");
        newOrder.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        orderDAO.createOrder(newOrder);

        for (OrderDetailModel item : items) {
            item.setOrderId(newOrder.getId());
            orderDAO.createOrderItem(item);
        }

        if (isApiRequest(request)) {
            utils.HttpUtil.sendJson(response, HttpServletResponse.SC_CREATED, newOrder);
        } else {
            response.sendRedirect("orders");
        }
    }

    private void updateOrder(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        double amount = Double.parseDouble(request.getParameter("amount"));
        
        OrderModel existingOrder = orderDAO.getOrderById(id);
        if (existingOrder != null && "PENDING".equals(existingOrder.getStatus())) {
            existingOrder.setAmount(amount);
            orderDAO.updateOrder(existingOrder);
        }
        response.sendRedirect("orders");
    }

    private void deleteOrder(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        OrderModel existingOrder = orderDAO.getOrderById(id);
        if (existingOrder != null && "PENDING".equals(existingOrder.getStatus())) {
            orderDAO.deleteOrder(id);
            if (isApiRequest(request)) {
                utils.HttpUtil.sendJson(response, HttpServletResponse.SC_OK, "Order deleted successfully");
            } else {
                response.sendRedirect("orders");
            }
        } else {
            if (isApiRequest(request)) {
                utils.HttpUtil.sendJson(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot delete order (not PENDING or not found)");
            } else {
                response.sendRedirect("orders");
            }
        }
    }

    private void viewOrderDetails(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect("orders");
            return;
        }

        OrderModel order = null;
        try {
            // Thử tìm theo ID số trước
            int id = Integer.parseInt(idParam);
            order = orderDAO.getOrderById(id);
        } catch (NumberFormatException e) {
            // Nếu không phải số, tìm theo mã đơn hàng (orderCode)
            order = orderDAO.getOrderByCode(idParam);
        }

        if (order != null) {
            List<OrderDetailModel> items = orderDAO.getOrderItemsByOrderId(order.getId());
            if (isApiRequest(request)) {
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("order", order);
                data.put("items", items);
                utils.HttpUtil.sendJson(response, HttpServletResponse.SC_OK, data);
            } else {
                request.setAttribute("order", order);
                request.setAttribute("items", items);
                request.getRequestDispatcher("/orderDetail.jsp").forward(request, response);
            }
        } else {
            if (isApiRequest(request)) {
                utils.HttpUtil.sendJson(response, HttpServletResponse.SC_NOT_FOUND, "Order not found with identifier: " + idParam);
            } else {
                response.sendRedirect("orders");
            }
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept != null && accept.contains("application/json")) || 
               (request.getContentType() != null && request.getContentType().contains("application/json"));
    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
