<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Thanh toán QR</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 400px; margin: auto; text-align: center; border: 1px solid #ccc; padding: 20px; border-radius: 8px; }
        input { padding: 8px; margin-bottom: 10px; width: 80%; }
        button { padding: 8px 20px; cursor: pointer; background-color: #4CAF50; color: white; border: none; border-radius: 4px; }
        .qr-section { margin-top: 20px; }
        img { max-width: 250px; border: 1px solid #ddd; padding: 5px; }
        .info { font-weight: bold; margin-top: 10px; color: #333; }
        .status { margin-top: 15px; padding: 10px; font-weight: bold; border-radius: 4px; }
        .scanning { color: #856404; background-color: #fff3cd; border: 1px solid #ffeeba; }
        .found { color: #155724; background-color: #d4edda; border: 1px solid #c3e6cb; }
        .not-found { color: #721c24; background-color: #f8d7da; border: 1px solid #f5c6cb; }
        .scan-btn { background-color: #007bff; margin-top: 10px; }
    </style>
</head>
<body>

<div class="container">
    <h2>Thanh toán qua QR</h2>
    
    <form action="payment" method="post">
        <label for="amount">Nhập số tiền:</label><br>
        <!-- Chỉ cho phép nhập số -->
        <input type="number" id="amount" name="amount" required value="${amount != null ? amount : ''}">
        <br>
        <button type="submit">Tạo QR</button>
    </form>

    <%-- Hiển thị QR nếu có --%>
    <% if (request.getAttribute("qrUrl") != null || request.getParameter("orderId") != null) { 
        String qrUrl = (String) request.getAttribute("qrUrl");
        String transactionCode = (String) request.getAttribute("transactionCode");
        Object amount = request.getAttribute("amount");
        
        if (qrUrl != null) {
    %>
        <div class="qr-section">
            <p>Mã QR của bạn:</p>
            <img src="<%= qrUrl %>" alt="QR Code">
            <div class="info">
                Mã giao dịch: <span id="txnCode"><%= transactionCode %></span>
            </div>
            <div class="info">
                Số tiền: <%= amount %> VNĐ
            </div>

            <button type="button" class="scan-btn" id="startScanBtn" onclick="startPolling()">Bắt đầu quét giao dịch</button>
            <div id="statusBox" class="status" style="display: none;"></div>
        </div>
    <% } } %>
</div>

<script>
    let pollInterval;

    function startPolling() {
        const btn = document.getElementById('startScanBtn');
        const statusBox = document.getElementById('statusBox');
        const txnCode = document.getElementById('txnCode').innerText;

        btn.disabled = true;
        btn.innerText = "Đang quét...";
        
        statusBox.style.display = "block";
        statusBox.className = "status scanning";
        statusBox.innerText = "Đang quét giao dịch...";

        // Bắt đầu gọi AJAX mỗi 3 giây
        pollInterval = setInterval(() => {
            checkTransaction(txnCode);
        }, 3000);
    }

    function checkTransaction(txnCode) {
        const statusBox = document.getElementById('statusBox');

        fetch('check-transaction')
            .then(response => response.json())
            .then(data => {
                if (data.found) {
                    // Dừng quét khi tìm thấy
                    clearInterval(pollInterval);
                    
                    statusBox.className = "status found";
                    statusBox.innerText = data.message ? data.message : ("Đã phát hiện giao dịch mã " + txnCode);
                    
                    document.getElementById('startScanBtn').innerText = "Thanh toán hoàn tất";
                } else {
                    statusBox.className = "status scanning";
                    if (data.message) {
                        statusBox.innerText = data.message;
                    } else {
                        statusBox.innerText = "Đang quét... (Chưa tìm thấy giao dịch)";
                    }
                }
            })
            .catch(error => {
                console.error('Error:', error);
                statusBox.className = "status not-found";
                statusBox.innerText = "Lỗi khi kiểm tra giao dịch.";
            });
    }
</script>

</body>
</html>
