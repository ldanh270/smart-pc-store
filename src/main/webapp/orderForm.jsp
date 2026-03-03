<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>${order == null ? 'Thêm mới' : 'Cập nhật'} đơn hàng</title>
    <style>
        .product-list { margin: 15px 0; max-height: 400px; overflow-y: auto; border: 1px solid #ddd; padding: 10px; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h2>${order == null ? 'Thêm mới' : 'Cập nhật'} đơn hàng</h2>
    <form action="orders" method="post">
        <input type="hidden" name="action" value="${order == null ? 'insert' : 'update'}">
        <c:if test="${order != null}">
            <input type="hidden" name="id" value="${order.id}">
            <p>Mã đơn hàng: ${order.orderCode}</p>
        </c:if>

        <c:choose>
            <c:when test="${order == null}">
                <p>Chọn sản phẩm và nhập số lượng:</p>
                <div class="product-list">
                    <table>
                        <tr>
                            <th>Chọn</th>
                            <th>Tên sản phẩm</th>
                            <th>Giá</th>
                            <th>Kho</th>
                            <th>Số lượng</th>
                        </tr>
                        <c:forEach var="p" items="${products}">
                            <tr>
                                <td><input type="checkbox" name="productIds" value="${p.id}"></td>
                                <td>${p.productName}</td>
                                <td>${p.currentPrice}</td>
                                <td>${p.quantity}</td>
                                <td><input type="number" name="qty_${p.id}" value="0" min="0" style="width: 60px;"></td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <p>
                    Số tiền: 
                    <input type="number" name="amount" value="${order.amount}" required 
                        ${order.status != 'PENDING' ? 'disabled' : ''}>
                </p>
                <p>Trạng thái: ${order.status}</p>
            </c:otherwise>
        </c:choose>

        <p>
            <input type="submit" value="Lưu">
            <a href="orders">Hủy</a>
        </p>
    </form>
</body>
</html>
