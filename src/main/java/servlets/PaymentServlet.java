package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dao.JPAUtil;
import dao.OrderDao;
import dao.PaymentDao;
import entities.Payment;
import enums.PaymentMethod;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PaymentService;
import services.payment.PaymentException;
import utils.HttpUtil; // Assuming HttpUtil exists for JSON responses

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PaymentServlet handles HTTP requests related to payments.
 * URL Patterns:
 * - /payments/create (POST)
 * - /payments/{id} (GET)
 * - /payments/{id}/callback (POST)
 */
@WebServlet(name = "PaymentServlet", urlPatterns = {"/payments/*"})
public class PaymentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PaymentServlet.class.getName());
    private Gson gson = new Gson();
    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        // Initialize DAOs and Service
        PaymentDao paymentDao = new PaymentDao(JPAUtil.getEntityManager());
        OrderDao orderDao = new OrderDao(JPAUtil.getEntityManager()); // Assuming OrderDao exists
        this.paymentService = new PaymentService(paymentDao, orderDao);
        LOGGER.log(Level.INFO, "PaymentServlet initialized.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // E.g., "/create", "/{id}/callback"

        if (pathInfo == null || pathInfo.equals("/")) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"Invalid payment endpoint\"}");
            return;
        }

        try {
            if (pathInfo.equals("/create")) {
                handleCreatePayment(req, resp);
            } else if (pathInfo.matches("/\\d+/callback")) { // Matches /payments/{id}/callback
                handlePaymentCallback(req, resp);
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "{\"message\": \"Endpoint not found\"}");
            }
        } catch (JsonSyntaxException e) {
            LOGGER.log(Level.WARNING, "Invalid JSON format in request body", e);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"Invalid JSON format\"}");
        } catch (PaymentException e) {
            LOGGER.log(Level.WARNING, "Payment processing error: " + e.getMessage(), e);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"" + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid ID format in URL", e);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"Invalid ID format in URL\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Internal server error in PaymentServlet", e);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "{\"message\": \"Internal Server Error\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // E.g., "/{id}"

        if (pathInfo == null || pathInfo.equals("/")) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"Invalid payment endpoint\"}");
            return;
        }

        try {
            if (pathInfo.matches("/\\d+")) { // Matches /payments/{id}
                handleGetPaymentStatus(req, resp);
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "{\"message\": \"Endpoint not found\"}");
            }
        } catch (PaymentException e) {
            LOGGER.log(Level.WARNING, "Payment status retrieval error: " + e.getMessage(), e);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "{\"message\": \"" + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid ID format in URL", e);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"Invalid ID format in URL\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Internal server error in PaymentServlet", e);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "{\"message\": \"Internal Server Error\"}");
        }
    }

    private void handleCreatePayment(HttpServletRequest req, HttpServletResponse resp) throws IOException, PaymentException {
        // Input: {"orderId": 123, "paymentMethod": "COD"}
        JsonObject requestBody = gson.fromJson(req.getReader(), JsonObject.class);

        Integer orderId = requestBody.has("orderId") ? requestBody.get("orderId").getAsInt() : null;
        String paymentMethodStr = requestBody.has("paymentMethod") ? requestBody.get("paymentMethod").getAsString() : null;

        if (orderId == null || paymentMethodStr == null || paymentMethodStr.isEmpty()) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"Missing orderId or paymentMethod\"}");
            return;
        }

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"Invalid payment method: " + paymentMethodStr + "\"}");
            return;
        }

        Payment payment = paymentService.initiatePayment(orderId, paymentMethod);

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", true);
        responseJson.addProperty("message", "Payment initiated successfully");
        responseJson.addProperty("paymentId", payment.getId());
        // For external gateways, you might add a redirectUrl here
        // responseJson.addProperty("redirectUrl", "...");

        HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, responseJson.toString());
        LOGGER.log(Level.INFO, "Payment initiated: {0}", payment.getId());
    }

    private void handleGetPaymentStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException, PaymentException {
        String pathInfo = req.getPathInfo(); // E.g., "/123"
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 2) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"Missing payment ID\"}");
            return;
        }
        Integer paymentId = Integer.parseInt(pathParts[1]);

        Payment payment = paymentService.getPaymentStatus(paymentId);

        // Convert Payment object to JSON (exclude sensitive data if any)
        String paymentJson = gson.toJson(payment);
        HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, paymentJson);
        LOGGER.log(Level.INFO, "Payment status retrieved for ID: {0}", payment.getId());
    }

    private void handlePaymentCallback(HttpServletRequest req, HttpServletResponse resp) throws IOException, PaymentException {
        String pathInfo = req.getPathInfo(); // E.g., "/123/callback"
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 3) { // Expecting /{id}/callback
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"message\": \"Invalid callback URL format\"}");
            return;
        }
        Integer paymentId = Integer.parseInt(pathParts[1]);

        // In a real scenario, you'd determine the payment method from the paymentId
        // or from the callback data itself, and then verify the callback signature.
        // For this example, we'll assume the payment method is known or can be retrieved.
        Payment existingPayment = paymentService.getPaymentStatus(paymentId);
        PaymentMethod paymentMethod = existingPayment.getPaymentMethod();

        // Parse callback data (this will vary greatly by payment gateway)
        // For simplicity, let's assume the callback sends a JSON with "status" and "transactionRef"
        JsonObject callbackData = gson.fromJson(req.getReader(), JsonObject.class);

        // TODO: IMPORTANT: Implement robust signature verification for real gateways!
        // Example: if (!verifySignature(req, callbackData)) {
        //     HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "{\"message\": \"Invalid callback signature\"}");
        //     return;
        // }

        Payment updatedPayment = paymentService.processPaymentCallback(paymentId, paymentMethod, callbackData);

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("status", "received");
        responseJson.addProperty("message", "Callback processed for payment ID: " + updatedPayment.getId());
        HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, responseJson.toString());
        LOGGER.log(Level.INFO, "Payment callback processed for ID: {0}, new status: {1}",
                new Object[]{updatedPayment.getId(), updatedPayment.getPaymentStatus()});
    }
}
