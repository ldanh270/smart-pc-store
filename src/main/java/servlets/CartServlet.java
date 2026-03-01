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

/**
 * CartServlet
 * Endpoints:
 * - GET    /cart/               => get cart items
 * - POST   /cart/add            => add product to cart
 * - PUT    /cart/items/{id}     => update quantity of an item
 * - DELETE /cart/               => clear cart (after checkout)
 * - DELETE /cart/items/{id}     => remove one item
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart/*"})
public class CartServlet extends HttpServlet {

    // Dependency Injection
    private CartController cartController;

    @Override
    public void init() {
        UserDao userDao = new UserDao();
        CartDao cartDao = new CartDao();
        CartItemDao cartItemDao = new CartItemDao();
        GenericDao<Product> productDao = new GenericDao<>(Product.class);
        CartService cartService = new CartService(userDao, cartDao, cartItemDao, productDao);
        this.cartController = new CartController(cartService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try (EntityManager em = JPAUtil.getEntityManager()) {
            // GET /cart or GET /cart/
            if (pathInfo == null || "/".equals(pathInfo)) {
                cartController.handleGetCart(req, resp);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            System.err.println("ERROR CartServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // Keep response format consistent by using HttpUtil.sendJson
        if (pathInfo == null || "/".equals(pathInfo)) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing endpoint path");
            return;
        }

        try (EntityManager em = JPAUtil.getEntityManager()) {
            switch (pathInfo) {
                case "/add" -> cartController.handleAddToCart(req, resp);
                default -> HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            System.err.println("ERROR CartServlet - doPost: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // expected: /items/{id}
        try (EntityManager em = JPAUtil.getEntityManager()) {
            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                Integer cartItemId = Integer.parseInt(pathInfo.substring("/items/".length()));
                cartController.handleUpdateQuantity(req, resp, cartItemId);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            System.err.println("ERROR CartServlet - doPut: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Internal server error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try (EntityManager em = JPAUtil.getEntityManager()) {
            // DELETE /cart/ => clear entire cart (typically after checkout)
            if (pathInfo == null || "/".equals(pathInfo)) {
                cartController.handleClearCart(req, resp);
                return;
            }

            // DELETE /cart/items/{id} => remove one item
            if (pathInfo.startsWith("/items/")) {
                Integer cartItemId = Integer.parseInt(pathInfo.substring("/items/".length()));
                cartController.handleRemoveItem(req, resp, cartItemId);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            System.err.println("ERROR CartServlet - doDelete: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Internal server error");
        }
    }
}
