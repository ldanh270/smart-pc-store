package services;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import dao.OrderDAO;
import dao.OrderDetailDao;
import dao.ProductDao;
import dto.order.CreateOrderRequestDto;
import dto.order.OrderDetailDto;
import dto.order.OrderResponseDto;
import dto.order.OrderViewResponseDto;
import entities.Order;
import entities.OrderDetail;
import entities.Product;
import utils.NumberUtil;

public class OrderService {

    private static final String SEPAY_QR_BASE = "https://qr.sepay.vn/img";
    private static final String SEPAY_QR_ACCOUNT = "VQRQAELYF2308";
    private static final String SEPAY_QR_BANK = "MBBank";

    private final OrderDAO orderDao;
    private final OrderDetailDao orderDetailDao;
    private final ProductDao productDao;

    public OrderService(OrderDAO orderDao, OrderDetailDao orderDetailDao, ProductDao productDao) {
        this.orderDao = orderDao;
        this.orderDetailDao = orderDetailDao;
        this.productDao = productDao;
    }

    public OrderResponseDto createOrder(CreateOrderRequestDto dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất 1 sản phẩm");
        }

        double totalAmount = 0;
        List<OrderDetail> details = new ArrayList<>();

        for (CreateOrderRequestDto.OrderItemRequestDto itemDto : dto.getItems()) {
            Product product = productDao.findById(itemDto.getProductId());
            if (product != null && itemDto.getQuantity() > 0) {
                double unitPrice = product.getCurrentPrice().doubleValue();
                totalAmount += unitPrice * itemDto.getQuantity();

                OrderDetail detail = new OrderDetail();
                detail.setProduct(product);
                detail.setQuantity(itemDto.getQuantity());
                detail.setUnitPrice(BigDecimal.valueOf(unitPrice));
                details.add(detail);
            }
        }

        if (details.isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm hợp lệ để tạo đơn hàng");
        }

        Order order = new Order();
        order.setOrderCode("DH" + System.currentTimeMillis() % 1000000);
        order.setAmount(totalAmount);
        order.setTransactionCode(generateRandomString(8));
        order.setStatus("PENDING");
        order.setCreatedAt(OffsetDateTime.now());

        try {
            orderDao.getEntityManager().getTransaction().begin();
            orderDao.create(order);
            for (OrderDetail detail : details) {
                detail.setOrder(order);
                orderDetailDao.create(detail);
            }
            orderDao.getEntityManager().getTransaction().commit();
        } catch (Exception e) {
            if (orderDao.getEntityManager().getTransaction().isActive()) {
                orderDao.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }

        return mapToResponse(order);
    }

    public List<OrderResponseDto> getAllOrders() {
        return getAllOrders(null, null, null);
    }

    public List<OrderResponseDto> getAllOrders(String q, Integer page, Integer size) {
        return orderDao.searchAndPaginate(q, page, size).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<OrderResponseDto> getUserOrders(UUID userId, Integer page, Integer size) {
        return orderDao.findByUserId(userId, page, size).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public OrderViewResponseDto getOrderDetails(String identifier) {
        Order order;
        if (NumberUtil.isNumeric(identifier)) {
            order = orderDao.findById(Integer.valueOf(identifier));
        } else {
            order = orderDao.findSingleByOrderCode(identifier);
        }

        if (order == null) {
            return null;
        }

        OrderViewResponseDto response = new OrderViewResponseDto();
        response.setOrder(mapToResponse(order));

        if ("PENDING".equals(order.getStatus())) {
            response.setQrCode(buildQrUrl(order));
        }

        List<OrderDetail> details = orderDetailDao.findByOrderId(order.getId());
        response.setItems(details.stream().map(this::mapToDetailDto).collect(Collectors.toList()));

        return response;
    }

    public void deleteOrder(String identifier) {
        Order order;
        if (utils.NumberUtil.isNumeric(identifier)) {
            order = orderDao.findById(Integer.valueOf(identifier));
        } else {
            order = orderDao.findSingleByOrderCode(identifier);
        }

        if (order != null && "PENDING".equals(order.getStatus())) {
            try {
                orderDao.getEntityManager().getTransaction().begin();
                // Delete details first
                List<OrderDetail> details = orderDetailDao.findByOrderId(order.getId());
                for (OrderDetail d : details) {
                    orderDetailDao.delete(d.getId());
                }
                orderDao.delete(order.getId());
                orderDao.getEntityManager().getTransaction().commit();
            } catch (Exception e) {
                if (orderDao.getEntityManager().getTransaction().isActive()) {
                    orderDao.getEntityManager().getTransaction().rollback();
                }
                throw e;
            }
        } else if (order == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng");
        } else {
            throw new IllegalArgumentException("Chỉ được xóa đơn hàng ở trạng thái PENDING");
        }
    }

    public void cancelOrder(String identifier, UUID userId) {
        Order order;
        if (utils.NumberUtil.isNumeric(identifier)) {
            order = orderDao.findById(Integer.valueOf(identifier));
        } else {
            order = orderDao.findSingleByOrderCode(identifier);
        }

        if (order == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng");
        }
        
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new SecurityException("Forbidden");
        }

        if ("PENDING".equals(order.getStatus())) {
            try {
                orderDao.getEntityManager().getTransaction().begin();
                order.setStatus("CANCELLED");
                orderDao.update(order);
                orderDao.getEntityManager().getTransaction().commit();
            } catch (Exception e) {
                if (orderDao.getEntityManager().getTransaction().isActive()) {
                    orderDao.getEntityManager().getTransaction().rollback();
                }
                throw e;
            }
        } else {
            throw new IllegalArgumentException("Chỉ được hủy đơn hàng ở trạng thái PENDING");
        }
    }

    private OrderResponseDto mapToResponse(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderCode(order.getOrderCode());
        dto.setAmount(order.getAmount());
        dto.setTransactionCode(order.getTransactionCode());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt().toInstant());
        return dto;
    }

    private OrderDetailDto mapToDetailDto(OrderDetail detail) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(detail.getId());
        dto.setProductId(detail.getProduct().getId());
        dto.setProductName(detail.getProduct().getProductName());
        dto.setQuantity(detail.getQuantity());
        dto.setUnitPrice(detail.getUnitPrice());
        return dto;
    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
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
}
