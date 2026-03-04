package servlets;

import controllers.PaymentController;
import dao.OrderDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PaymentService;
import utils.HttpUtil;

import java.io.IOException;

@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment/*"})
public class PaymentServlet extends HttpServlet {
    private PaymentController paymentController;

    @Override
    public void init() {
        OrderDao orderDao = new OrderDao();
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
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
}
