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
import java.util.UUID;

/**
 * ProductServlet handles product-related HTTP requests.
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/products/*"})
public class ProductServlet extends HttpServlet {

    // Dependency Injection
    private ProductController productController;

    @Override
    public void init() {
        ProductDao productDao = new ProductDao();
        ProductService productService = new ProductService(productDao);
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

            // GET /products/{id} or /products/{slug}
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                String identifier = parts[1];
                try {
                    // Try to parse as UUID first
                    UUID.fromString(identifier);
                    productController.handleGetById(req, resp, identifier);
                } catch (IllegalArgumentException e) {
                    // Not a UUID, handle as slug
                    productController.handleGetBySlug(req, resp, identifier);
                }
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            System.err.println("ERROR ProductServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // Keep create route explicit to avoid conflicts with future POST actions.
        if (pathInfo == null || pathInfo.equals("/")) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found. To create a product, POST to /products/create");
            return;
        }

        try {
            // Routing
            if (pathInfo.equals("/create")) {
                productController.handleCreate(req, resp);
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            System.err.println("ERROR ProductServlet - doPost: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
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
            System.err.println("ERROR ProductServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
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
            System.err.println("ERROR ProductServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }
}
