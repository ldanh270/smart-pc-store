package servlets;

import controllers.UserController;
import dao.GenericDao;
import dao.JPAUtil;
import dao.UserDao;
import entities.User;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.UserService;
import utils.HttpUtil;

import java.io.IOException;

/**
 * UserServlet
 *
 * Routes: - GET /users => list users - GET /users/{id} => detail - POST /users
 * => create - PUT /users/{id} => update - DELETE /users/{id} => delete
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/users/*"})
public class UserServlet extends HttpServlet {

    // Dependency Injection
    private UserController userController;

    @Override
    public void init() {
        UserDao userDao = new UserDao();
        UserService cartService = new UserService(userDao);
        this.userController = new UserController(cartService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo(); // null or "/{id}"

            if (pathInfo == null || "/".equals(pathInfo)) {
                userController.handleGetAll(req, resp);
                return;
            }

            // GET /users/{id}
            Integer id = Integer.valueOf(pathInfo.substring(1));
            userController.handleGetById(req, resp, id);

        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user id");
        } catch (IOException e) {
            System.err.println("ERROR UserServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try (EntityManager em = JPAUtil.getEntityManager()) {
            if (pathInfo != null && !"/".equals(pathInfo)) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                return;
            }
            userController.handleCreate(req, resp);
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // "/{id}"
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing user id");
                return;
            }
            Integer id = Integer.valueOf(pathInfo.substring(1));
            userController.handleUpdate(req, resp, id);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user id");
        } catch (IOException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // "/{id}"
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing user id");
                return;
            }
            Integer id = Integer.valueOf(pathInfo.substring(1));
            userController.handleDelete(req, resp, id);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user id");
        } catch (IOException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
