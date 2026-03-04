package servlets;

import java.io.IOException;

import controllers.PaymentController;
import dao.OrderDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PaymentService;
import utils.HttpUtil;

/**
 * PaymentServlet handles payment-related requests
 */
@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment/*"})
public class PaymentServlet extends HttpServlet {

    private PaymentController paymentController;

    @Override
    public void init() {
        OrderDAO orderDao = new OrderDAO();
        PaymentService paymentService = new PaymentService(orderDao);
        this.paymentController = new PaymentController(paymentService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/qr")) {
                paymentController.handleGetQr(req, resp);
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (IOException e) {
            System.err.println("ERROR PaymentServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
}
