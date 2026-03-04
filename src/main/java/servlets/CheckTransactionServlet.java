package servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.OrderDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.PaymentService;
import utils.HttpUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@WebServlet(name = "CheckTransactionServlet", urlPatterns = {"/check-transaction"})
public class CheckTransactionServlet extends HttpServlet {

    private static final String API_TOKEN = "Bearer E7EQFSUFXWLBW1NMNQ6HPUBJF8WTYMY0RDXYJONAT72IIBSPO52ZOHM1QCRO9WQZ";
    private static final String SEPAY_API_URL = "https://my.sepay.vn/userapi/transactions/list";
    
    private PaymentService paymentService;

    @Override
    public void init() {
        OrderDao orderDao = new OrderDao();
        this.paymentService = new PaymentService(orderDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String currentCode = request.getParameter("txnCode");
        if (currentCode == null || currentCode.isEmpty()) {
            HttpSession session = request.getSession();
            currentCode = (String) session.getAttribute("currentTransactionCode");
        }

        if (currentCode == null || currentCode.isEmpty()) {
            HttpUtil.sendJson(response, HttpServletResponse.SC_BAD_REQUEST, "No transaction code provided");
            return;
        }

        final String finalCode = currentCode;

        try {
            String jsonResponse = callSePayApi();
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray transactions = root.getAsJsonArray("transactions");

            boolean found = false;
            String message = "Chưa tìm thấy giao dịch";
            
            if (transactions != null) {
                for (JsonElement element : transactions) {
                    JsonObject txn = element.getAsJsonObject();
                    double amountIn = txn.get("amount_in").getAsDouble();
                    String content = txn.get("transaction_content").getAsString();

                    if (amountIn > 0 && content != null && content.contains(finalCode)) {
                        paymentService.updateStatus(finalCode, "PAID");
                        found = true;
                        message = "đã phát hiện giao dịch mã " + finalCode + ". đơn hàng đã được thanh toán";
                        break;
                    }
                }
            }

            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("found", found);
            result.put("message", message);
            HttpUtil.sendJson(response, HttpServletResponse.SC_OK, result);

        } catch (Exception e) {
            HttpUtil.sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

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
