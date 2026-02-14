package servlets;

import controllers.CartController;
import dao.*;
import entities.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.CartService;
import utils.HttpUtil;

import java.io.IOException;

@WebServlet(name = "CartServlet", urlPatterns = {"/cart/*"})
public class CartServlet extends HttpServlet {

    private CartController cartController;

    @Override
    public void init() {
        // Dùng CHUNG 1 EntityManager cho các DAO trong Cart
        var em = JPAUtil.getEntityManager();

        UserDao userDao = new UserDao(em);
        CartDao cartDao = new CartDao(em);
        CartItemDao cartItemDao = new CartItemDao(em);
        GenericDao<Product> productDao = new GenericDao<>(Product.class, em);

        CartService cartService = new CartService(userDao, cartDao, cartItemDao, productDao);
        this.cartController = new CartController(cartService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                cartController.handleGetCart(req, resp);
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
        if (pathInfo == null || "/".equals(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            switch (pathInfo) {
                case "/add":
                    cartController.handleAddToCart(req, resp);
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
        try {
            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                Integer cartItemId = Integer.parseInt(pathInfo.substring("/items/".length()));
                cartController.handleUpdateQuantity(req, resp, cartItemId);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // /items/123
        try {
            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                Integer cartItemId = Integer.parseInt(pathInfo.substring("/items/".length()));
                cartController.handleRemoveItem(req, resp, cartItemId);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
