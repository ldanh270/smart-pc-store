/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlets;

import controllers.AuthController;
import dao.JPAUtil;
import dao.SessionDao;
import dao.UserDao;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.AuthService;
import utils.HttpUtil;

import java.io.IOException;

/**
 * AuthServlet handles authentication-related HTTP requests.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/*"})
public class AuthServlet extends HttpServlet {
    private AuthController buildController(EntityManager em) {
        UserDao userDAO = new UserDao(em);
        SessionDao sessionDAO = new SessionDao(em);
        AuthService authService = new AuthService(userDAO, sessionDAO);
        return new AuthController(authService);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        EntityManager em = JPAUtil.getEntityManager();
        AuthController authController = buildController(em);

        // Routing
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            if (em.isOpen()) em.close();
            return;
        }

        try {
            switch (pathInfo) {
                case "/login":
                    authController.handleLogin(req, resp);
                    break;
                case "/signup":
                    authController.handleSignup(req, resp);
                    break;
                case "/refresh":
                    authController.handleRefreshToken(req, resp);
                    break;
                case "/logout":
                    authController.handleLogout(req, resp);
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
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}
