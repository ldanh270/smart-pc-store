/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlets;

import java.io.IOException;

import controllers.CategoryController;
import dao.CategoryDao;
import dao.ProductDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.CategoryService;
import services.ProductService;
import utils.HttpUtil;

/**
 *
 * @author ducan
 */
@WebServlet(name = "CategoryServlet", urlPatterns = {"/categories/*"})
public class CategoryServlet extends HttpServlet {

    // Dependency Injection
    private CategoryController categoryController;

    @Override
    public void init() {
        CategoryDao categoryDao = new CategoryDao();
        ProductDao productDao = new ProductDao();
        ProductService productService = new ProductService(productDao);
        CategoryService categoryService = new CategoryService(categoryDao, productService);
        this.categoryController = new CategoryController(categoryService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            // Routing:
            // GET /categories or /categories/ -> list/filter categories
            if (pathInfo == null || pathInfo.equals("/")) {
                categoryController.handleGetAll(req, resp);
                return;
            }

            // GET /categories/{id} -> category details
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                categoryController.handleGetById(req, resp, parts[1]);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            System.err.println("ERROR CategoryServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        // Keep create route explicit to avoid conflicts with future POST actions.
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            // Routing: POST /categories/create
            switch (pathInfo) {
                case "/create" ->
                    categoryController.handleCreate(req, resp);
                default ->
                    HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            System.err.println("ERROR CategoryServlet - doPost: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            // Routing: PUT /categories/{id} -> update category
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                categoryController.handleUpdate(req, resp, parts[1]);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            System.err.println("ERROR CategoryServlet - doPut: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            // Routing: DELETE /categories/{id}
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                categoryController.handleDelete(req, resp, parts[1]);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            System.err.println("ERROR CategoryServlet - doDelete: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }
}
