package servlets;

import java.io.IOException;

import controllers.OrderController;
import dao.OrderDAO;
import dao.OrderDetailDao;
import dao.ProductDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.OrderService;
import utils.HttpUtil;

@WebServlet(name = "OrderServlet", urlPatterns = {"/orders/*"})
public class OrderServlet extends HttpServlet {

    private OrderController orderController;

    @Override
    public void init() {
        OrderDAO orderDao = new OrderDAO();
        OrderDetailDao orderDetailDao = new OrderDetailDao();
        ProductDao productDao = new ProductDao();
        OrderService orderService = new OrderService(orderDao, orderDetailDao, productDao);
        this.orderController = new OrderController(orderService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            String action = req.getParameter("action");

            if (pathInfo == null || pathInfo.equals("/")) {
                if (null == action) {
                    orderController.handleGetAll(req, resp);
                } else {
                    switch (action) {
                        case "view" ->
                            orderController.handleGetDetail(req, resp);
                        case "delete" ->
                            orderController.handleDelete(req, resp);
                        case "my-orders" ->
                            orderController.handleGetMyOrders(req, resp);
                        default ->
                            orderController.handleGetAll(req, resp);
                    }
                }
                return;
            }

            switch (pathInfo) {
                case "/detail" ->
                    orderController.handleGetDetail(req, resp);
                case "/delete" ->
                    orderController.handleDelete(req, resp);
                case "/my-orders" ->
                    orderController.handleGetMyOrders(req, resp);
                default ->
                    HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (IOException e) {
            System.err.println("ERROR OrderServlet - doGet: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null) {
                switch (pathInfo) {
                    case "/create" ->
                        orderController.handleCreate(req, resp);
                    case "/cancel" ->
                        orderController.handleCancelOrder(req, resp);
                    default ->
                        HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                }
            } else {
                HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (IOException e) {
            System.err.println("ERROR OrderServlet - doPost: " + e.getMessage());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
}
