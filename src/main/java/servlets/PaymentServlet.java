package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Random;

/**
 * Servlet xử lý tạo mã giao dịch và build link thanh toán QR.
 */
@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String orderIdentifier = request.getParameter("orderId");
        if (orderIdentifier != null && !orderIdentifier.isEmpty()) {
            try {
                dao.OrderDAO orderDAO = new dao.OrderDAO();
                entities.OrderModel order = null;
                
                try {
                    // 1. Thử tìm theo ID số (khóa chính)
                    int id = Integer.parseInt(orderIdentifier);
                    order = orderDAO.getOrderById(id);
                } catch (NumberFormatException e) {
                    // Không phải số -> Chuyển sang tìm theo mã đơn hàng (orderCode)
                    order = null; 
                }
                
                // 2. Nếu tìm theo ID không ra (hoặc không phải số), thử tìm theo mã DHxxxx
                if (order == null) {
                    order = orderDAO.getOrderByCode(orderIdentifier);
                }

                if (order != null) {
                    String transactionCode = order.getTransactionCode();
                    double amount = order.getAmount();
                    
                    String qrUrl = String.format(
                        "https://qr.sepay.vn/img?acc=VQRQAELYF2308&bank=MBBank&amount=%.0f&des=%s",
                        amount, transactionCode
                    );
                    
                    if (isApiRequest(request)) {
                        java.util.Map<String, Object> result = new java.util.HashMap<>();
                        result.put("amount", amount);
                        result.put("transactionCode", transactionCode);
                        result.put("qrUrl", qrUrl);
                        utils.HttpUtil.sendJson(response, HttpServletResponse.SC_OK, result);
                        return;
                    } else {
                        request.setAttribute("amount", amount);
                        request.setAttribute("transactionCode", transactionCode);
                        request.setAttribute("qrUrl", qrUrl);
                        request.getSession().setAttribute("currentTransactionCode", transactionCode);
                    }
                } else if (isApiRequest(request)) {
                    utils.HttpUtil.sendJson(response, HttpServletResponse.SC_NOT_FOUND, "Order not found with identifier: " + orderIdentifier);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isApiRequest(request)) {
                    utils.HttpUtil.sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
                    return;
                }
            }
        }
        // Chuyển hướng người dùng đến trang JSP khi truy cập GET /payment
        request.getRequestDispatcher("/testPayment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 1. Lấy số tiền từ input (amount)
            String amountStr = request.getParameter("amount");
            
            // Nếu là request JSON (từ Postman/AJAX), có thể amount nằm trong body
            if (amountStr == null && request.getContentType() != null && request.getContentType().contains("application/json")) {
                java.util.Map<String, Object> body = new com.google.gson.Gson().fromJson(request.getReader(), java.util.Map.class);
                if (body != null && body.get("amount") != null) {
                    amountStr = body.get("amount").toString();
                }
            }

            if (amountStr == null || amountStr.isEmpty()) {
                if (isApiRequest(request)) {
                    utils.HttpUtil.sendJson(response, HttpServletResponse.SC_BAD_REQUEST, "Amount is required");
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Amount is required");
                }
                return;
            }
            
            double amount = Double.parseDouble(amountStr);
            // 2. Tạo chuỗi ngẫu nhiên 8 ký tự (chữ hoa, chữ thường, số)
            String transactionCode = generateRandomString(8);

            // --- Tích hợp tạo Order mới vào DB ---
            dao.OrderDAO orderDAO = new dao.OrderDAO();
            entities.OrderModel newOrder = new entities.OrderModel();
            String orderCode = "DH" + (System.currentTimeMillis() % 1000000);
            newOrder.setOrderCode(orderCode);
            newOrder.setAmount(amount);
            newOrder.setTransactionCode(transactionCode);
            newOrder.setStatus("PENDING");
            newOrder.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            orderDAO.createOrder(newOrder);
            
            // Lưu orderId vào session theo yêu cầu
            request.getSession().setAttribute("orderId", newOrder.getId());
            // ------------------------------------
            
            // 3. Build link QR theo format:
            // https://qr.sepay.vn/img?acc=VQRQAELYF2308&bank=MBBank&amount=SO_TIEN&des=MA_GIAO_DICH
            String qrUrl = String.format(
                "https://qr.sepay.vn/img?acc=VQRQAELYF2308&bank=MBBank&amount=%.0f&des=%s",
                amount, transactionCode
            );
            
            // Lưu mã giao dịch vào Session để servlet kiểm tra có thể lấy ra đối chiếu
            request.getSession().setAttribute("currentTransactionCode", transactionCode);

            // 4. Trả về kết quả tùy theo loại request
            if (isApiRequest(request)) {
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("amount", amount);
                result.put("transactionCode", transactionCode);
                result.put("qrUrl", qrUrl);
                utils.HttpUtil.sendJson(response, HttpServletResponse.SC_OK, result);
            } else {
                // Gửi dữ liệu sang JSP để hiển thị
                request.setAttribute("qrUrl", qrUrl);
                request.setAttribute("transactionCode", transactionCode);
                request.setAttribute("amount", amount);
                request.getRequestDispatcher("/testPayment.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isApiRequest(request)) {
                utils.HttpUtil.sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Error: " + e.getMessage());
            } else {
                request.setAttribute("errorMessage", "Error creating payment: " + e.getMessage());
                request.getRequestDispatcher("/error.jsp").forward(request, response);
            }
        }
    }

    /**
     * Kiểm tra xem request có phải là API request (Postman, AJAX) hay không.
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept != null && accept.contains("application/json")) || 
               (request.getContentType() != null && request.getContentType().contains("application/json"));
    }

    /**
     * Hàm sinh chuỗi ngẫu nhiên gồm chữ hoa, chữ thường và số.
     */
    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        
        return sb.toString();
    }
}
