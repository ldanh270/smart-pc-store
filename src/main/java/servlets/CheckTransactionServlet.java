package servlets;

import java.io.IOException;
import java.util.UUID;

import dao.CartDao;
import dao.CartItemDao;
import dao.JPAUtil;
import dao.OrderDAO;
import dao.OrderDetailDao;
import dao.ProductDao;
import dao.UserDao;
import dto.payment.PurchaseCheckResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.PurchaseCheckoutService;
import utils.HttpUtil;

@WebServlet(name = "CheckTransactionServlet", urlPatterns = {"/check-transaction"})
public class CheckTransactionServlet extends HttpServlet {

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UUID userId = (UUID) request.getAttribute("userId");
        if (userId == null) {
            HttpUtil.sendJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        String userRole = (String) request.getAttribute("userRole");

        String transactionCode = request.getParameter("txnCode");
        if (transactionCode == null || transactionCode.isBlank()) {
            transactionCode = request.getParameter("transactionCode");
        }
        if (transactionCode == null || transactionCode.isBlank()) {
            HttpUtil.sendJson(response, HttpServletResponse.SC_BAD_REQUEST, "No transaction code provided");
            return;
        }

        try {
            PurchaseCheckResponseDto result = purchaseCheckoutService.getOrderCheckStatus(
                    transactionCode,
                    userId,
                    userRole
            );
            HttpUtil.sendJson(response, HttpServletResponse.SC_OK, result);
        } catch (SecurityException e) {
            HttpUtil.sendJson(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }
}
