package servlets;

import java.io.IOException;

import controllers.SupplierController;
import dao.JPAUtil;
import dao.SupplierDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.SupplierService;
import utils.HttpUtil;

/**
 * SupplierServlet handles supplier-related HTTP requests.
 */
@WebServlet(name = "SupplierServlet", urlPatterns = {"/suppliers/*"})
public class SupplierServlet extends HttpServlet {

    private SupplierController buildController() {
        SupplierDao supplierDao = new SupplierDao();
        SupplierService supplierService = new SupplierService(supplierDao);
        return new SupplierController(supplierService);
    }

    /**
     * Route supplier GET endpoints.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        SupplierController supplierController = buildController();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                supplierController.handleGetAll(req, resp);
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                supplierController.handleGetById(resp, parts[1]);
                return;
            }

            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
            System.err.println("ERROR SupplierServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        } finally {
            JPAUtil.closeEntityManager();
        }
    }

    /**
     * Route supplier POST endpoints.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        SupplierController supplierController = buildController();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            JPAUtil.closeEntityManager();
            return;
        }

        try {
            if (pathInfo.equals("/create")) {
                supplierController.handleCreate(req, resp);
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (IOException e) {
            System.err.println("ERROR SupplierServlet - doPost: " + e.getMessage());
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        } finally {
            JPAUtil.closeEntityManager();
        }
    }

    /**
     * Route supplier PUT endpoints.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        SupplierController supplierController = buildController();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            JPAUtil.closeEntityManager();
            return;
        }

        try {
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                supplierController.handleUpdate(req, resp, parts[1]);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        } finally {
            JPAUtil.closeEntityManager();
        }
    }

    /**
     * Route supplier DELETE endpoints.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        SupplierController supplierController = buildController();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            JPAUtil.closeEntityManager();
            return;
        }

        try {
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                supplierController.handleDelete(resp, parts[1]);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
            HttpUtil.sendJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage()
            );
        } finally {
            JPAUtil.closeEntityManager();
        }
    }
}
