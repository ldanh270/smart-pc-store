<%-- src/main/webapp/WEB-INF/views/checkout/paymentForm.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="enums.PaymentMethod" %>
<!DOCTYPE html>
<html>
<head>
    <title>Chọn phương thức thanh toán</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .payment-option { margin-bottom: 10px; }
        .payment-option label { margin-left: 5px; }
        .btn { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; }
        .btn:hover { background-color: #0056b3; }
        #paymentResult { margin-top: 20px; padding: 10px; border: 1px solid #ccc; background-color: #f9f9f9; }
    </style>
</head>
<body>
    <h1>Chọn phương thức thanh toán</h1>

    <%-- Giả lập một Order ID. Trong ứng dụng thực tế, ID này sẽ đến từ session hoặc request attributes --%>
    <% Integer orderId = (Integer) request.getAttribute("orderId");
       if (orderId == null) {
           // Dành cho mục đích demo, sử dụng một ID đơn hàng giả
           orderId = 123; // Thay thế bằng ID đơn hàng thực tế từ luồng ứng dụng của bạn
       }
    %>
    <p>Đơn hàng của bạn: <strong>#<%= orderId %></strong></p>
    <p>Tổng tiền: <strong><%= request.getAttribute("totalAmount") != null ? request.getAttribute("totalAmount") : "123.45" %> VND</strong></p>

    <form id="paymentForm">
        <input type="hidden" id="orderId" name="orderId" value="<%= orderId %>">

        <div class="payment-option">
            <input type="radio" id="cod" name="paymentMethod" value="<%= PaymentMethod.COD.name() %>" checked>
            <label for="cod">Thanh toán khi nhận hàng (COD)</label>
        </div>
        <div class="payment-option">
            <input type="radio" id="bankTransfer" name="paymentMethod" value="<%= PaymentMethod.BANK_TRANSFER.name() %>">
            <label for="bankTransfer">Chuyển khoản ngân hàng</label>
        </div>
        <div class="payment-option">
            <input type="radio" id="paypal" name="paymentMethod" value="<%= PaymentMethod.PAYPAL.name() %>">
            <label for="paypal">PayPal</label>
        </div>
        <%-- Thêm nhiều phương thức thanh toán khác tại đây --%>

        <button type="submit" class="btn">Xác nhận thanh toán</button>
    </form>

    <div id="paymentResult">
        Kết quả thanh toán sẽ hiển thị ở đây.
    </div>

    <script>
        document.getElementById('paymentForm').addEventListener('submit', async function(event) {
            event.preventDefault(); // Ngăn chặn gửi form mặc định

            const orderId = document.getElementById('orderId').value;
            const selectedMethod = document.querySelector('input[name="paymentMethod"]:checked').value;

            // Điều chỉnh context path nếu cần (ví dụ: /smart-pc-store/payments/create)
            const response = await fetch('/smart-pc-store/payments/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    orderId: parseInt(orderId),
                    paymentMethod: selectedMethod
                })
            });

            const result = await response.json();
            const paymentResultDiv = document.getElementById('paymentResult');

            if (response.ok) {
                paymentResultDiv.innerHTML = `
                    <p style="color: green;">${result.message}</p>
                    <p>Mã giao dịch: <strong>${result.paymentId}</strong></p>
                    <p>Phương thức: <strong>${selectedMethod}</strong></p>
                    <p>Bạn có thể kiểm tra trạng thái tại: <a href="/smart-pc-store/payments/${result.paymentId}">/payments/${result.paymentId}</a></p>
                    ${result.redirectUrl ? `<p>Chuyển hướng đến cổng thanh toán: <a href="${result.redirectUrl}">${result.redirectUrl}</a></p>` : ''}
                `;
                // Đối với các cổng bên ngoài, bạn có thể chuyển hướng ở đây:
                // if (result.redirectUrl) {
                //     window.location.href = result.redirectUrl;
                // }
            } else {
                paymentResultDiv.innerHTML = `<p style="color: red;">Lỗi: ${result.message || 'Không xác định'}</p>`;
            }
        });
    </script>
</body>
</html>
