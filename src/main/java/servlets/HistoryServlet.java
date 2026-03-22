package servlets;

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

import java.io.IOException;

@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        orderController.handleGetMyOrders(request, response);
    }
}
