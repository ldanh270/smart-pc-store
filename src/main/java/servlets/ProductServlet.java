package servlets;

import controllers.ProductController;
import dao.JPAUtil;
import dao.ProductDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.ProductService;
import utils.HttpUtil;

import java.io.IOException;

/**
 * ProductServlet handles product-related HTTP requests.
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/products/*"})
public class ProductServlet extends HttpServlet {

    // Dependency Injection
    private ProductController productController;

    @Override
    public void init() {
        var em = JPAUtil.getEntityManager();
        ProductDao productDao = new ProductDao(em);
        ProductService productService = new ProductService(productDao, em);
        this.productController = new ProductController(productService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            // Routing:
            // GET /products or /products/ -> list/filter products
            if (pathInfo == null || pathInfo.equals("/")) {
                productController.handleGetAll(req, resp);
                return;
            }

            // GET /products/{id} -> product details
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                productController.handleGetById(req, resp, parts[1]);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // Keep create route explicit to avoid conflicts with future POST actions.
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            // Routing
            switch (pathInfo) {
                case "/create":
                    productController.handleCreate(req, resp);
                    break;
                default:
                    HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            // Routing:
            // PUT /products/{id} -> update product
            // PUT /products/{id}/adjust -> adjust stock
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                productController.handleUpdate(req, resp, parts[1]);
                return;
            }
            if (parts.length == 3 && !parts[1].isBlank() && "adjust".equals(parts[2])) {
                productController.handleAdjustStock(req, resp, parts[1]);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            // Routing: DELETE /products/{id}
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                productController.handleDelete(req, resp, parts[1]);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        }
    }
}
