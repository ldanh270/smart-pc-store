package servlets;

import controllers.CartController;
import dao.*;
import entities.Product;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.CartService;
import utils.HttpUtil;

import java.io.IOException;

@WebServlet(name = "CartServlet", urlPatterns = { "/cart/*" })
public class CartServlet extends HttpServlet {

    /**
     * Tạo CartController mới mỗi request với EntityManager riêng.
     * Fix thread-safety: không dùng chung 1 EntityManager cho tất cả request.
     */
    private CartController buildController(EntityManager em) {
        UserDao userDao = new UserDao(em);
        CartDao cartDao = new CartDao(em);
        CartItemDao cartItemDao = new CartItemDao(em);
        GenericDao<Product> productDao = new GenericDao<>(Product.class, em);
        CartService cartService = new CartService(userDao, cartDao, cartItemDao, productDao);
        return new CartController(cartService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try (EntityManager em = JPAUtil.getEntityManager()) {
            if (pathInfo == null || "/".equals(pathInfo)) {
                buildController(em).handleGetCart(req, resp);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        // Fix: dùng HttpUtil.sendJson thay vì sendError để response nhất quán
        if (pathInfo == null || "/".equals(pathInfo)) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing endpoint path");
            return;
        }

        try (EntityManager em = JPAUtil.getEntityManager()) {
            switch (pathInfo) {
                case "/add":
                    buildController(em).handleAddToCart(req, resp);
                    break;
                default:
                    HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // /items/123
        try (EntityManager em = JPAUtil.getEntityManager()) {
            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                Integer cartItemId = Integer.parseInt(pathInfo.substring("/items/".length()));
                buildController(em).handleUpdateQuantity(req, resp, cartItemId);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try (EntityManager em = JPAUtil.getEntityManager()) {
            // DELETE /cart/ → xóa toàn bộ giỏ hàng (dùng khi Checkout hoàn tất)
            if (pathInfo == null || "/".equals(pathInfo)) {
                buildController(em).handleClearCart(req, resp);
                return;
            }
            // DELETE /cart/items/123 → xóa 1 item
            if (pathInfo.startsWith("/items/")) {
                Integer cartItemId = Integer.parseInt(pathInfo.substring("/items/".length()));
                buildController(em).handleRemoveItem(req, resp, cartItemId);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
