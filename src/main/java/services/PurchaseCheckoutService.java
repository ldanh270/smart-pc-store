package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.CartDao;
import dao.CartItemDao;
import dao.OrderDAO;
import dao.OrderDetailDao;
import dao.ProductDao;
import dao.UserDao;
import dto.payment.PaymentResponseDto;
import dto.payment.PurchaseCheckResponseDto;
import dto.payment.PurchaseCheckoutRequestDto;
import entities.Cart;
import entities.CartItem;
import entities.Order;
import entities.OrderDetail;
import entities.Product;
import entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import utils.EnvHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class PurchaseCheckoutService {

    private static final String ORDER_STATUS_PENDING = "PENDING";
    private static final String ORDER_STATUS_PAID = "PAID";
    private static final String SEPAY_QR_BASE = "https://qr.sepay.vn/img";
    private static final String SEPAY_QR_ACCOUNT = "VQRQAELYF2308";
    private static final String SEPAY_QR_BANK = "MBBank";

    private final OrderDAO orderDao;
    private final OrderDetailDao orderDetailDao;
    private final ProductDao productDao;
    private final UserDao userDao;
    private final CartDao cartDao;
    private final CartItemDao cartItemDao;

    public PurchaseCheckoutService(
            OrderDAO orderDao,
            OrderDetailDao orderDetailDao,
            ProductDao productDao,
            UserDao userDao,
            CartDao cartDao,
            CartItemDao cartItemDao
    ) {
        this.orderDao = orderDao;
        this.orderDetailDao = orderDetailDao;
        this.productDao = productDao;
        this.userDao = userDao;
        this.cartDao = cartDao;
        this.cartItemDao = cartItemDao;
    }

    public PaymentResponseDto createPendingOrder(PurchaseCheckoutRequestDto dto) {
        UUID userId = dto == null ? null : dto.resolveUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        User user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (dto.getProducts() == null || dto.getProducts().isEmpty()) {
            throw new IllegalArgumentException("products must not be empty");
        }

        double totalAmount = 0d;
        List<OrderDetail> details = new ArrayList<>();

        for (PurchaseCheckoutRequestDto.PurchaseProductDto item : dto.getProducts()) {
            if (item == null || item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }

            Product product = productDao.findById(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + item.getProductId());
            }

            Integer stock = product.getQuantity();
            if (stock != null && stock < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getProductName());
            }

            double unitPrice = product.getCurrentPrice() == null ? 0d : product.getCurrentPrice().doubleValue();
            totalAmount += unitPrice * item.getQuantity();

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(BigDecimal.valueOf(unitPrice));
            details.add(detail);
        }

        if (details.isEmpty()) {
            throw new IllegalArgumentException("No valid products to create order");
        }

        Order order = new Order();
        order.setOrderCode(generateUniqueOrderCode());
        order.setTransactionCode(generateUniqueTransactionCode(10));
        order.setAmount(totalAmount);
        order.setStatus(ORDER_STATUS_PENDING);
        order.setCreatedAt(OffsetDateTime.now());
        order.setUser(user);

        EntityTransaction tx = orderDao.getEntityManager().getTransaction();
        try {
            tx.begin();
            orderDao.create(order);
            for (OrderDetail detail : details) {
                detail.setOrder(order);
                orderDetailDao.create(detail);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }

        return new PaymentResponseDto(order.getAmount(), order.getTransactionCode(), buildQrUrl(order));
    }

    public PurchaseCheckResponseDto checkTransactionAndCompleteOrder(
            String transactionCode,
            UUID requesterUserId,
            String requesterRole
    ) {
        return getOrderCheckStatus(transactionCode, requesterUserId, requesterRole);
    }

    public PurchaseCheckResponseDto getOrderCheckStatus(
            String transactionCode,
            UUID requesterUserId,
            String requesterRole
    ) {
        if (transactionCode == null || transactionCode.isBlank()) {
            throw new IllegalArgumentException("transactionCode is required");
        }
        if (requesterUserId == null) {
            throw new IllegalArgumentException("requester user is required");
        }

        Order order = orderDao.findSingleByTransactionCode(transactionCode);
        if (order == null) {
            throw new IllegalArgumentException("Order not found for transactionCode");
        }
        ensureCanAccessOrder(order, requesterUserId, requesterRole);
        if (ORDER_STATUS_PAID.equalsIgnoreCase(order.getStatus())) {
            return new PurchaseCheckResponseDto(transactionCode, order.getStatus(), true, "Order already PAID");
        }
        return new PurchaseCheckResponseDto(transactionCode, order.getStatus(), false, "Waiting for payment");
    }

    public int runSingleThreadCheckCycle(int limit) {
        List<Order> pendingOrders = orderDao.findByStatus(ORDER_STATUS_PENDING, limit);
        if (pendingOrders.isEmpty()) {
            return 0;
        }

        JsonArray transactions = fetchTransactions();
        int completedCount = 0;
        
        // Let's say orders older than 30 minutes are expired
        OffsetDateTime expirationTime = OffsetDateTime.now().minusMinutes(30);
        
        for (Order order : pendingOrders) {
            try {
                if (isPaidInTransactions(order, transactions)) {
                    completeOrder(order);
                    completedCount++;
                } else if (order.getCreatedAt() != null && order.getCreatedAt().isBefore(expirationTime)) {
                    expireOrder(order);
                }
            } catch (Exception e) {
                System.err.println(
                        "WARN: Failed to process pending order " + order.getTransactionCode() + ": " + e.getMessage()
                );
            }
        }
        return completedCount;
    }

    private void expireOrder(Order order) {
        EntityManager entityManager = orderDao.getEntityManager();
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            Order managedOrder = entityManager.find(Order.class, order.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (managedOrder == null || !ORDER_STATUS_PENDING.equalsIgnoreCase(managedOrder.getStatus())) {
                tx.rollback();
                return;
            }
            managedOrder.setStatus("EXPIRED");
            orderDao.update(managedOrder);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    private void completeOrder(Order order) {
        EntityManager entityManager = orderDao.getEntityManager();
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            Order managedOrder = entityManager.find(Order.class, order.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (managedOrder == null) {
                throw new IllegalStateException("Order not found while completing");
            }
            if (ORDER_STATUS_PAID.equalsIgnoreCase(managedOrder.getStatus())) {
                tx.commit();
                return;
            }

            List<OrderDetail> details = orderDetailDao.findByOrderId(managedOrder.getId());
            for (OrderDetail detail : details) {
                Product product = productDao.findById(detail.getProduct().getId());
                if (product == null) {
                    throw new IllegalStateException("Product not found while completing order");
                }

                Integer currentStock = product.getQuantity() == null ? 0 : product.getQuantity();
                if (currentStock < detail.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock when completing order");
                }

                product.setQuantity(currentStock - detail.getQuantity());
                productDao.update(product);
            }

            removePurchasedItemsFromCart(managedOrder, details);

            managedOrder.setStatus(ORDER_STATUS_PAID);
            orderDao.update(managedOrder);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    private void removePurchasedItemsFromCart(Order order, List<OrderDetail> details) {
        User user = order.getUser();
        if (user == null) {
            return;
        }

        Cart cart = cartDao.findByUser(user);
        if (cart == null) {
            return;
        }

        for (OrderDetail detail : details) {
            CartItem cartItem = cartItemDao.findByCartAndProduct(cart, detail.getProduct());
            if (cartItem == null) {
                continue;
            }

            int remain = cartItem.getQuantity() - detail.getQuantity();
            if (remain <= 0) {
                cartItemDao.delete(cartItem.getId());
            } else {
                cartItem.setQuantity(remain);
                cartItemDao.update(cartItem);
            }
        }
    }

    private JsonArray fetchTransactions() {
        try {
            String jsonResponse = callSePayApi();
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray transactions = root.getAsJsonArray("transactions");
            return transactions == null ? new JsonArray() : transactions;
        } catch (IOException e) {
            throw new RuntimeException("Cannot call SePay: " + e.getMessage(), e);
        }
    }

    private boolean isPaidInTransactions(Order order, JsonArray transactions) {
        String orderCode = order.getOrderCode();
        String transactionCode = order.getTransactionCode();
        long expectedAmount = Math.round(order.getAmount() == null ? 0d : order.getAmount());
        for (JsonElement element : transactions) {
            JsonObject txn = element.getAsJsonObject();
            String content = safeGetString(txn, "transaction_content");
            double amountIn = safeGetDouble(txn, "amount_in");
            boolean matchDescription = content != null
                    && ((orderCode != null && content.contains(orderCode))
                    || (transactionCode != null && content.contains(transactionCode)));
            long transferredAmount = Math.round(amountIn);
            if (transferredAmount >= expectedAmount && matchDescription) {
                return true;
            }
        }
        return false;
    }

    private String callSePayApi() throws IOException {
        String sePayApiUrl = EnvHelper.get("SEPAY_API_URL", "https://my.sepay.vn/userapi/transactions/list");
        String apiToken = EnvHelper.get("SEPAY_API_TOKEN");
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalStateException("Missing SEPAY_API_TOKEN");
        }

        URL url = new URL(sePayApiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", normalizeBearerToken(apiToken));
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }

        throw new IOException("SePay API returned HTTP code: " + responseCode);
    }

    private String normalizeBearerToken(String token) {
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return token;
        }
        return "Bearer " + token;
    }

    private String buildQrUrl(Order order) {
        String description = URLEncoder.encode(order.getOrderCode(), StandardCharsets.UTF_8);
        return String.format(
                "%s?acc=%s&bank=%s&amount=%.0f&des=%s",
                SEPAY_QR_BASE,
                SEPAY_QR_ACCOUNT,
                SEPAY_QR_BANK,
                order.getAmount(),
                description
        );
    }

    private String generateUniqueOrderCode() {
        String code;
        do {
            code = "DH" + System.currentTimeMillis() + randomDigits(4);
        } while (orderDao.findSingleByOrderCode(code) != null);
        return code;
    }

    private String generateUniqueTransactionCode(int length) {
        String code;
        do {
            code = randomAlphaNumeric(length);
        } while (orderDao.findSingleByTransactionCode(code) != null);
        return code;
    }

    private String randomAlphaNumeric(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private String randomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void ensureCanAccessOrder(Order order, UUID requesterUserId, String requesterRole) {
        boolean isAdmin = requesterRole != null && "ADMIN".equalsIgnoreCase(requesterRole);
        if (isAdmin) {
            return;
        }
        if (order.getUser() == null || order.getUser().getId() == null || !order.getUser().getId().equals(requesterUserId)) {
            throw new SecurityException("Forbidden");
        }
    }

    private String safeGetString(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    private double safeGetDouble(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return 0d;
        }
        return element.getAsDouble();
    }
}
