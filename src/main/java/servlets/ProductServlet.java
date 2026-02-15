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

@WebServlet(name = "ProductServlet", urlPatterns = {"/product/*"})
public class ProductServlet extends HttpServlet {

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

        if (pathInfo == null || pathInfo.equals("/")) {
            productController.handleGetAll(req, resp);
            return;
        }

        String[] parts = pathInfo.split("/");

        if (parts.length == 2) {
            productController.handleGetById(req, resp, parts[1]);
        } else {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if ("/create".equals(pathInfo)) {
            productController.handleCreate(req, resp);
        } else {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length == 2) {
            productController.handleUpdate(req, resp, parts[1]);
        } else if (parts.length == 3 && "adjust".equals(parts[2])) {
            // PUT /product/{id}/adjust
            productController.handleAdjustStock(req, resp, parts[1]);
        } else {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length == 2) {
            productController.handleDelete(req, resp, parts[1]);
        } else {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }
}
