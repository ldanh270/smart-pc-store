package servlets;

import controllers.UserController;
import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.UserService;
import utils.HttpUtil;

import java.io.IOException;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    private UserController userController;

    @Override
    public void init() {
        UserDao userDao = new UserDao();
        UserService userService = new UserService(userDao);
        this.userController = new UserController(userService);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            HttpUtil.sendJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        userController.handleGetById(request, response, userId);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            HttpUtil.sendJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        userController.handleUpdate(request, response, userId);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        doPut(request, response);
    }
}
