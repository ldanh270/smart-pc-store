package servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * CheckTransactionServlet handles checking transaction status via SePay API.
 * Endpoint: GET /check-transaction
 */
@WebServlet("/check-transaction")
public class CheckTransactionServlet extends HttpServlet {

    // API Token của SePay (Giữ bí mật ở phía server)
    private static final String API_TOKEN = "Bearer E7EQFSUFXWLBW1NMNQ6HPUBJF8WTYMY0RDXYJONAT72IIBSPO52ZOHM1QCRO9WQZ";
    private static final String SEPAY_API_URL = "https://my.sepay.vn/userapi/transactions/list";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 1. Lấy mã giao dịch cần kiểm tra (ưu tiên từ query param, sau đó từ session)
        String currentCode = request.getParameter("txnCode");
        if (currentCode == null || currentCode.isEmpty()) {
            HttpSession session = request.getSession();
            currentCode = (String) session.getAttribute("currentTransactionCode");
        }

        // Nếu chưa có mã giao dịch
        if (currentCode == null || currentCode.isEmpty()) {
            response.getWriter().write("{\"found\": false, \"message\": \"No transaction code provided or in session\"}");
            return;
        }

        final String finalCode = currentCode; // Dùng cho lambda if needed

        try {
            // 1. Gọi API SePay
            String jsonResponse = callSePayApi();
            
            // 2. Parse JSON bằng Gson (đã có trong pom.xml)
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray transactions = root.getAsJsonArray("transactions");

            boolean found = false;
            String message = "";
            if (transactions != null) {
                for (JsonElement element : transactions) {
                    JsonObject txn = element.getAsJsonObject();
                    
                    // Lấy số tiền thu vào (amount_in)
                    double amountIn = txn.get("amount_in").getAsDouble();
                    
                    // Lấy nội dung chuyển khoản (transaction_content)
                    String content = txn.get("transaction_content").getAsString();

                    // Logic: Chỉ kiểm tra tiền vào > 0 và nội dung chứa mã giao dịch (des)
                    if (amountIn > 0 && content != null && content.contains(finalCode)) {
                        // Tìm order tương ứng trong DB
                        dao.OrderDAO orderDAO = new dao.OrderDAO();
                        entities.OrderModel order = orderDAO.getOrderByTransactionCode(finalCode);
                        
                        if (order != null && "PENDING".equals(order.getStatus())) {
                            if (amountIn >= order.getAmount()) {
                                order.setStatus("PAID");
                                orderDAO.updateOrder(order);
                                found = true;
                                message = "đã phát hiện giao dịch mã " + finalCode + ". đơn hàng đã được thanh toán";
                            } else {
                                message = "Cảnh báo: Số tiền thanh toán (" + amountIn + ") nhỏ hơn số tiền đơn hàng (" + order.getAmount() + ")";
                            }
                        } else if (order != null && "PAID".equals(order.getStatus())) {
                            found = true;
                            message = "đã phát hiện giao dịch mã " + finalCode + ". đơn hàng đã được thanh toán";
                        }
                        break;
                    }
                }
            }

            // 3. Trả về kết quả cho AJAX
            JsonObject jsonRes = new JsonObject();
            jsonRes.addProperty("found", found);
            if (!message.isEmpty()) {
                jsonRes.addProperty("message", message);
            }
            response.getWriter().write(jsonRes.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Failed to check transaction: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Hàm gọi API SePay lấy danh sách giao dịch gần đây.
     */
    private String callSePayApi() throws IOException {
        URL url = new URL(SEPAY_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", API_TOKEN);
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new IOException("SePay API returned HTTP code: " + responseCode);
        }
    }
}
