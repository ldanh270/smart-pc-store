package servlets;

import controllers.SupplierController;
import dao.JPAUtil;
import dao.SupplierDao;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.SupplierService;
import utils.HttpUtil;

import java.io.IOException;

/**
 * SupplierServlet handles supplier-related HTTP requests.
 */
@WebServlet(name = "SupplierServlet", urlPatterns = {"/suppliers/*"})
public class SupplierServlet extends HttpServlet {
    private SupplierController buildController(EntityManager em) {
        SupplierDao supplierDao = new SupplierDao(em);
        SupplierService supplierService = new SupplierService(supplierDao, em);
        return new SupplierController(supplierService);
    }

    /**
     * Route supplier GET endpoints.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        SupplierController supplierController = buildController(em);
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
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    /**
     * Route supplier POST endpoints.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        SupplierController supplierController = buildController(em);
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            if (em.isOpen()) em.close();
            return;
        }

        try {
            switch (pathInfo) {
                case "/create":
                    supplierController.handleCreate(req, resp);
                    break;
                default:
                    HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    /**
     * Route supplier PUT endpoints.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        SupplierController supplierController = buildController(em);
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            if (em.isOpen()) em.close();
            return;
        }

        try {
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                supplierController.handleUpdate(req, resp, parts[1]);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    /**
     * Route supplier DELETE endpoints.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        SupplierController supplierController = buildController(em);
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            if (em.isOpen()) em.close();
            return;
        }

        try {
            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                supplierController.handleDelete(resp, parts[1]);
                return;
            }
            HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}
