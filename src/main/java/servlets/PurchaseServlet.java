package servlets;

import dao.CartDao;
import dao.CartItemDao;
import dao.JPAUtil;
import dao.OrderDAO;
import dao.OrderDetailDao;
import dao.ProductDao;
import dao.UserDao;
import dto.payment.PaymentResponseDto;
import dto.payment.PurchaseCheckoutRequestDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PurchaseCheckoutService;
import utils.HttpUtil;

import java.io.IOException;
import java.util.UUID;

@WebServlet(name = "PurchaseServlet", urlPatterns = {"/purchase/*"})
public class PurchaseServlet extends HttpServlet {

    private PurchaseCheckoutService purchaseCheckoutService;

    @Override
    public void init() {
        purchaseCheckoutService = new PurchaseCheckoutService(
                new OrderDAO(),
                new OrderDetailDao(),
                new ProductDao(),
                new UserDao(),
                new CartDao(),
                new CartItemDao()
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                UUID userId = (UUID) req.getAttribute("userId");
                if (userId == null) {
                    HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    return;
                }
                PurchaseCheckoutRequestDto dto = HttpUtil.jsonToClass(req.getReader(), PurchaseCheckoutRequestDto.class);
                if (dto == null) {
                    dto = new PurchaseCheckoutRequestDto();
                }
                dto.setUserId(userId);
                PaymentResponseDto payment = purchaseCheckoutService.createPendingOrder(dto);
                HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, payment);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }
}
