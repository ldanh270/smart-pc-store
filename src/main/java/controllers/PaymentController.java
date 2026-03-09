package controllers;

import java.io.IOException;

import dto.payment.PaymentResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PaymentService;
import utils.HttpUtil;

public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void handleGetQr(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("orderId");
        if (id == null || id.isEmpty()) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Order ID or Code is required");
            return;
        }
        PaymentResponseDto qrInfo = paymentService.getQrInfo(id);
        if (qrInfo == null) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Order not found");
        } else {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, qrInfo);
        }
    }
}
