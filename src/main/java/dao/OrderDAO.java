package dao;

import entities.OrderDetailModel;
import entities.OrderModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private Connection getConnection() throws SQLException {
        // Lấy thông tin kết nối từ persistence.xml hoặc dùng giá trị mặc định cho SQL Server
        String url = "jdbc:sqlserver://localhost:1433;databaseName=SMART_PC_STORE;encrypt=true;trustServerCertificate=true";
        String user = "sa";
        String pass = "123456";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server Driver not found", e);
        }
        return DriverManager.getConnection(url, user, pass);
    }

    public void createOrder(OrderModel order) throws SQLException {
        String sql = "INSERT INTO Orders (orderCode, amount, transactionCode, status, createdAt) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getOrderCode());
            ps.setDouble(2, order.getAmount());
            ps.setString(3, order.getTransactionCode());
            ps.setString(4, order.getStatus());
            ps.setTimestamp(5, order.getCreatedAt());
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                order.setId(rs.getInt(1));
            }
        }
    }

    public void createOrderItem(OrderDetailModel item) throws SQLException {
        String sql = "INSERT INTO OrderDetails (OrderId, ProductId, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.executeUpdate();
        }
    }

    public List<OrderModel> getOrders(int page, int pageSize, String searchCode) throws SQLException {
        List<OrderModel> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql = "SELECT * FROM Orders WHERE orderCode LIKE ? ORDER BY createdAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + (searchCode == null ? "" : searchCode) + "%");
            ps.setInt(2, offset);
            ps.setInt(3, pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        }
        return list;
    }

    public List<OrderModel> getAllOrders() throws SQLException {
        List<OrderModel> list = new ArrayList<>();
        String sql = "SELECT * FROM Orders ORDER BY createdAt DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        }
        return list;
    }

    public int getTotalOrders(String searchCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Orders WHERE orderCode LIKE ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + (searchCode == null ? "" : searchCode) + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public OrderModel getOrderById(int id) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToOrder(rs);
        }
        return null;
    }

    public OrderModel getOrderByCode(String orderCode) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE orderCode = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToOrder(rs);
        }
        return null;
    }

    public OrderModel getOrderByTransactionCode(String transactionCode) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE transactionCode = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToOrder(rs);
        }
        return null;
    }

    public void updateOrder(OrderModel order) throws SQLException {
        String sql = "UPDATE Orders SET amount = ?, status = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, order.getAmount());
            ps.setString(2, order.getStatus());
            ps.setInt(3, order.getId());
            ps.executeUpdate();
        }
    }

    public void deleteOrder(int id) throws SQLException {
        String sql = "DELETE FROM Orders WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<OrderDetailModel> getOrderItemsByOrderId(int orderId) throws SQLException {
        List<OrderDetailModel> list = new ArrayList<>();
        String sql = "SELECT oi.*, p.ProductName FROM OrderDetails oi " +
                     "JOIN Products p ON oi.ProductId = p.Id " +
                     "WHERE oi.OrderId = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderDetailModel item = new OrderDetailModel();
                item.setId(rs.getInt("Id"));
                item.setOrderId(rs.getInt("OrderId"));
                item.setProductId(rs.getInt("ProductId"));
                item.setProductName(rs.getString("ProductName"));
                item.setQuantity(rs.getInt("Quantity"));
                item.setUnitPrice(rs.getDouble("UnitPrice"));
                list.add(item);
            }
        }
        return list;
    }

    private OrderModel mapResultSetToOrder(ResultSet rs) throws SQLException {
        return new OrderModel(
            rs.getInt("id"),
            rs.getString("orderCode"),
            rs.getDouble("amount"),
            rs.getString("transactionCode"),
            rs.getString("status"),
            rs.getTimestamp("createdAt")
        );
    }
}
