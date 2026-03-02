package servlets;

import controllers.UserController;
import dao.JPAUtil;
import dao.JPAUtil;
import dao.UserDao;
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
 * Routes:
 * - GET /users => list users
 * - GET /users/{id} => detail
 * - POST /users => create
 * - PUT /users/{id} => update
 * - DELETE /users/{id} => delete
 */
@WebServlet(name = "UserServlet", urlPatterns = { "/users/*" })
public class UserServlet extends HttpServlet {

    private UserController buildController(EntityManager em) {
        UserDao userDao = new UserDao(em);
        UserService userService = new UserService(userDao);
        return new UserController(userService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // null or "/{id}"
        try (EntityManager em = JPAUtil.getEntityManager()) {
            UserController controller = buildController(em);

            if (pathInfo == null || "/".equals(pathInfo)) {
                controller.handleGetAll(req, resp);
                return;
            }

            // GET /users/{id}
            Integer id = Integer.parseInt(pathInfo.substring(1));
            controller.handleGetById(req, resp, id);

        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user id");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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
            buildController(em).handleCreate(req, resp);
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // "/{id}"
        try (EntityManager em = JPAUtil.getEntityManager()) {
            if (pathInfo == null || "/".equals(pathInfo)) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing user id");
                return;
            }
            Integer id = Integer.parseInt(pathInfo.substring(1));
            buildController(em).handleUpdate(req, resp, id);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user id");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // "/{id}"
        try (EntityManager em = JPAUtil.getEntityManager()) {
            if (pathInfo == null || "/".equals(pathInfo)) {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing user id");
                return;
            }
            Integer id = Integer.parseInt(pathInfo.substring(1));
            buildController(em).handleDelete(req, resp, id);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user id");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}