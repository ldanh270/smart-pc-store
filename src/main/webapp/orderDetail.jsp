<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Chi tiết đơn hàng</title>
    <style>
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f2f2f2; }
        .order-info { margin-bottom: 20px; padding: 15px; background: #f9f9f9; border-radius: 5px; }
        .back-link { margin-top: 20px; display: inline-block; }
    </style>
</head>
<body>
    <h2>Chi tiết đơn hàng</h2>

    <div class="order-info">
        <p><strong>Mã đơn hàng:</strong> ${order.orderCode}</p>
        <p><strong>Ngày tạo:</strong> ${order.createdAt}</p>
        <p><strong>Trạng thái:</strong> ${order.status}</p>
        <p><strong>Tổng tiền:</strong> ${order.amount} VNĐ</p>
    </div>

    <h3>Danh sách sản phẩm</h3>
    <table>
        <thead>
            <tr>
                <th>Sản phẩm</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="item" items="${items}">
                <tr>
                    <td>${item.productName}</td>
                    <td>${item.quantity}</td>
                    <td>${item.unitPrice}</td>
                    <td>${item.quantity * item.unitPrice}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <a href="orders" class="back-link"> Quay lại danh sách</a>
</body>
</html>
