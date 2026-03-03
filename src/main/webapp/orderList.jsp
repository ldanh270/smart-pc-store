<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Danh sách đơn hàng</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .pagination { margin-top: 20px; }
        .pagination a { padding: 8px 16px; text-decoration: none; border: 1px solid #ddd; color: black; }
        .pagination a.active { background-color: #4CAF50; color: white; border: 1px solid #4CAF50; }
        .disabled { color: #ccc; pointer-events: none; cursor: default; }
    </style>
</head>
<body>
    <h2>Quản lý đơn hàng</h2>
    <form action="orders" method="get">
        Tìm theo mã đơn hàng: 
        <input type="text" name="searchCode" value="${searchCode}">
        <input type="submit" value="Tìm kiếm">
        <a href="orders">Reset</a>
    </form>
    <br>
    <a href="orders?action=new">Tạo đơn hàng mới</a>
    <br><br>
    <table>
        <tr>
            <th>ID</th>
            <th>Mã đơn hàng</th>
            <th>Số tiền</th>
            <th>Mã GD</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th>Thao tác</th>
        </tr>
        <c:forEach var="order" items="${orderList}">
            <tr>
                <td>${order.id}</td>
                <td>${order.orderCode}</td>
                <td>${order.amount}</td>
                <td>${order.transactionCode}</td>
                <td>${order.status}</td>
                <td>${order.createdAt}</td>
                <td>
                    <a href="orders?action=view&id=${order.id}">Chi tiết</a> | 
                    <c:choose>
                        <c:when test="${order.status == 'PAID'}">
                            <span class="disabled">Sửa</span> | 
                            <span class="disabled">Xóa</span>
                        </c:when>
                        <c:otherwise>
                            <a href="payment?orderId=${order.id}">Thanh toán</a> | 
                            <a href="orders?action=edit&id=${order.id}">Sửa</a> | 
                            <a href="orders?action=delete&id=${order.id}" onclick="return confirm('Bạn có chắc muốn xóa?')">Xóa</a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
    </table>

    <div class="pagination">
        <c:forEach var="i" begin="1" end="${totalPages}">
            <a href="orders?page=${i}&searchCode=${searchCode}" class="${i == currentPage ? 'active' : ''}">${i}</a>
        </c:forEach>
    </div>
</body>
</html>
