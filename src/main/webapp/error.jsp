<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Error</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; text-align: center; }
        .error-container { border: 1px solid #f5c6cb; background-color: #f8d7da; color: #721c24; padding: 20px; border-radius: 8px; display: inline-block; }
        h1 { margin-top: 0; }
        a { color: #004085; text-decoration: none; font-weight: bold; }
    </style>
</head>
<body>
    <div class="error-container">
        <h1>Oops! Có lỗi xảy ra.</h1>
        <p>${errorMessage != null ? errorMessage : "Đã có lỗi hệ thống xảy ra. Vui lòng thử lại sau."}</p>
        <p><a href="javascript:history.back()">Quay lại trang trước</a> hoặc <a href="${pageContext.request.contextPath}/orders">Về danh sách đơn hàng</a></p>
    </div>
</body>
</html>
