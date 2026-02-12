/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlets;

import controllers.AuthController;
import dao.JPAUtil;
import dao.SessionDao;
import dao.UserDao;
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

    // Dependency Injection
    private AuthController authController;

    @Override
    public void init() {
        UserDao userDAO = new UserDao(JPAUtil.getEntityManager());
        SessionDao sessionDAO = new SessionDao(JPAUtil.getEntityManager());
        AuthService authService = new AuthService(userDAO, sessionDAO);
        this.authController = new AuthController(authService);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // Routing
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
        }
    }
}
