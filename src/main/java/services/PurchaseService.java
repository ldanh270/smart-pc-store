package services;

import dao.*;
import dto.purchase.GoodsReceiptRequestDto;
import dto.purchase.GoodsReceiptResponseDto;
import dto.purchase.PurchaseOrderCreateRequestDto;
import dto.purchase.PurchaseOrderResponseDto;
import entities.*;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for purchasing workflow.
 * Handles PO creation, GRN processing, stock updates, and PO status transitions.
 */
public class PurchaseService {

    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_PARTIAL_RECEIVED = "PARTIAL_RECEIVED";
    private static final String STATUS_RECEIVED = "RECEIVED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final PurchaseOrderDao purchaseOrderDao;
    private final PurchaseOrderItemDao purchaseOrderItemDao;
    private final SupplierDao supplierDao;
    private final ProductDao productDao;
    private final SupplierPriceHistoryDao supplierPriceHistoryDao;
    private final GoodsReceiptNoteDao goodsReceiptNoteDao;
    private final GoodsReceiptNoteItemDao goodsReceiptNoteItemDao;
    private final InventoryTransactionDao inventoryTransactionDao;
    private final EntityManager em;

    /**
     * Constructor.
     */
    public PurchaseService(PurchaseOrderDao purchaseOrderDao,
                           PurchaseOrderItemDao purchaseOrderItemDao,
                           SupplierDao supplierDao,
                           ProductDao productDao,
                           SupplierPriceHistoryDao supplierPriceHistoryDao,
                           GoodsReceiptNoteDao goodsReceiptNoteDao,
                           GoodsReceiptNoteItemDao goodsReceiptNoteItemDao,
                           InventoryTransactionDao inventoryTransactionDao,
                           EntityManager em) {
        this.purchaseOrderDao = purchaseOrderDao;
        this.purchaseOrderItemDao = purchaseOrderItemDao;
        this.supplierDao = supplierDao;
        this.productDao = productDao;
        this.supplierPriceHistoryDao = supplierPriceHistoryDao;
        this.goodsReceiptNoteDao = goodsReceiptNoteDao;
        this.goodsReceiptNoteItemDao = goodsReceiptNoteItemDao;
        this.inventoryTransactionDao = inventoryTransactionDao;
        this.em = em;
    }

    /**
     * Create a purchase order with multiple items.
     * Uses provided unit price or latest quotation when unit price is missing.
     *
     * @param dto Purchase order request.
     * @return Created purchase order response.
     */
    public PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderCreateRequestDto dto) {
        validateCreatePo(dto);
        Supplier supplier = supplierDao.findById(dto.supplierId);
        if (supplier == null) throw new IllegalArgumentException("Supplier not found");

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setOrderDate(LocalDate.now());
        po.setExpectedDeliveryDate(parseDate(dto.expectedDeliveryDate));
        po.setStatus(STATUS_SENT);
        po.setPoCode("PO-" + System.currentTimeMillis());

        List<PurchaseOrderItem> poItems = new ArrayList<>();

        try {
            em.getTransaction().begin();
            purchaseOrderDao.create(po);

            for (PurchaseOrderCreateRequestDto.Item itemDto : dto.items) {
                Product product = productDao.findById(itemDto.productId);
                if (product == null) throw new IllegalArgumentException("Product not found: " + itemDto.productId);
                if (itemDto.quantity == null || itemDto.quantity <= 0) {
                    throw new IllegalArgumentException("Quantity must be > 0");
                }

                BigDecimal price = resolveUnitPrice(dto.supplierId, itemDto);
                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPo(po);
                item.setProduct(product);
                item.setQuantity(itemDto.quantity);
                item.setUnitPrice(price);
                purchaseOrderItemDao.create(item);
                poItems.add(item);
            }

            em.getTransaction().commit();
            return toPoDto(po, poItems);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Retrieve one purchase order with line items.
     *
     * @param poId Purchase order ID.
     * @return Purchase order response or null if not found.
     */
    public PurchaseOrderResponseDto getPurchaseOrder(Integer poId) {
        PurchaseOrder po = purchaseOrderDao.findById(poId);
        if (po == null) return null;
        List<PurchaseOrderItem> items = purchaseOrderItemDao.findByPoId(poId);
        return toPoDto(po, items);
    }

    /**
     * Receive goods for a purchase order and create GRN record.
     * Also updates product stock and writes inventory transactions.
     *
     * @param poId Purchase order ID.
     * @param dto Goods receipt request.
     * @return Goods receipt response.
     */
    public GoodsReceiptResponseDto receiveGoods(Integer poId, GoodsReceiptRequestDto dto) {
        if (dto == null || dto.items == null || dto.items.isEmpty()) {
            throw new IllegalArgumentException("At least one received item is required");
        }

        PurchaseOrder po = purchaseOrderDao.findById(poId);
        if (po == null) throw new IllegalArgumentException("Purchase order not found");
        if (STATUS_CANCELLED.equalsIgnoreCase(po.getStatus())) {
            throw new IllegalArgumentException("Cannot receive goods for cancelled PO");
        }
        if (STATUS_RECEIVED.equalsIgnoreCase(po.getStatus())) {
            throw new IllegalArgumentException("PO already fully received");
        }

        List<PurchaseOrderItem> poItems = purchaseOrderItemDao.findByPoId(poId);
        Map<Integer, PurchaseOrderItem> poItemByProduct = poItems.stream()
                .collect(Collectors.toMap(i -> i.getProduct().getId(), i -> i));

        GoodsReceiptNote grn = new GoodsReceiptNote();
        grn.setPo(po);
        grn.setReceiptDate(LocalDate.now());
        grn.setNote(dto.note);

        List<GoodsReceiptNoteItem> grnItems = new ArrayList<>();

        try {
            em.getTransaction().begin();
            goodsReceiptNoteDao.create(grn);

            for (GoodsReceiptRequestDto.Item itemDto : dto.items) {
                validateReceiveItem(itemDto);
                PurchaseOrderItem orderedItem = poItemByProduct.get(itemDto.productId);
                if (orderedItem == null) {
                    throw new IllegalArgumentException("Product " + itemDto.productId + " not found in PO");
                }

                int alreadyReceived = goodsReceiptNoteItemDao.sumReceivedQuantityByPoAndProduct(poId, itemDto.productId);
                int remaining = orderedItem.getQuantity() - alreadyReceived;
                if (itemDto.quantityReceived > remaining) {
                    throw new IllegalArgumentException("Received quantity exceeds remaining quantity for product " + itemDto.productId);
                }

                GoodsReceiptNoteItem grnItem = new GoodsReceiptNoteItem();
                grnItem.setGrn(grn);
                grnItem.setProduct(orderedItem.getProduct());
                grnItem.setQuantityReceived(itemDto.quantityReceived);
                grnItem.setUnitCost(itemDto.unitCost);
                goodsReceiptNoteItemDao.create(grnItem);
                grnItems.add(grnItem);

                Product product = orderedItem.getProduct();
                int oldQty = product.getQuantity() == null ? 0 : product.getQuantity();
                product.setQuantity(oldQty + itemDto.quantityReceived);
                productDao.update(product);

                InventoryTransaction tx = new InventoryTransaction();
                tx.setProduct(product);
                tx.setQuantityChange(itemDto.quantityReceived);
                tx.setTransactionType("IMPORT");
                tx.setTransactionDate(Instant.now());
                inventoryTransactionDao.create(tx);
            }

            po.setStatus(resolvePoStatus(poId, poItems));
            purchaseOrderDao.update(po);

            em.getTransaction().commit();
            return toGrnDto(grn, grnItems);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    private String resolvePoStatus(Integer poId, List<PurchaseOrderItem> poItems) {
        boolean allReceived = true;
        for (PurchaseOrderItem item : poItems) {
            int received = goodsReceiptNoteItemDao.sumReceivedQuantityByPoAndProduct(poId, item.getProduct().getId());
            if (received < item.getQuantity()) {
                allReceived = false;
                break;
            }
        }
        return allReceived ? STATUS_RECEIVED : STATUS_PARTIAL_RECEIVED;
    }

    private BigDecimal resolveUnitPrice(Integer supplierId, PurchaseOrderCreateRequestDto.Item itemDto) {
        if (itemDto.unitPrice != null && itemDto.unitPrice.compareTo(BigDecimal.ZERO) > 0) {
            return itemDto.unitPrice;
        }
        SupplierPriceHistory latest = supplierPriceHistoryDao.findLatest(itemDto.productId, supplierId);
        if (latest == null || latest.getImportPrice() == null) {
            throw new IllegalArgumentException("unitPrice is required when no quotation exists for product " + itemDto.productId);
        }
        return latest.getImportPrice();
    }

    private PurchaseOrderResponseDto toPoDto(PurchaseOrder po, List<PurchaseOrderItem> poItems) {
        PurchaseOrderResponseDto dto = new PurchaseOrderResponseDto();
        dto.id = po.getId();
        dto.poCode = po.getPoCode();
        dto.supplierId = po.getSupplier().getId();
        dto.supplierName = po.getSupplier().getSupplierName();
        dto.orderDate = po.getOrderDate() == null ? null : po.getOrderDate().toString();
        dto.expectedDeliveryDate = po.getExpectedDeliveryDate() == null ? null : po.getExpectedDeliveryDate().toString();
        dto.status = po.getStatus();

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseOrderResponseDto.Item> items = new ArrayList<>();
        for (PurchaseOrderItem poItem : poItems) {
            PurchaseOrderResponseDto.Item item = new PurchaseOrderResponseDto.Item();
            item.id = poItem.getId();
            item.productId = poItem.getProduct().getId();
            item.productName = poItem.getProduct().getProductName();
            item.quantity = poItem.getQuantity();
            item.unitPrice = poItem.getUnitPrice();
            item.lineTotal = poItem.getUnitPrice().multiply(BigDecimal.valueOf(poItem.getQuantity()));
            total = total.add(item.lineTotal);
            items.add(item);
        }
        dto.items = items;
        dto.totalAmount = total;
        return dto;
    }

    private GoodsReceiptResponseDto toGrnDto(GoodsReceiptNote grn, List<GoodsReceiptNoteItem> grnItems) {
        GoodsReceiptResponseDto dto = new GoodsReceiptResponseDto();
        dto.id = grn.getId();
        dto.poId = grn.getPo().getId();
        dto.receiptDate = grn.getReceiptDate() == null ? null : grn.getReceiptDate().toString();
        dto.note = grn.getNote();

        BigDecimal total = BigDecimal.ZERO;
        List<GoodsReceiptResponseDto.Item> items = new ArrayList<>();
        for (GoodsReceiptNoteItem grnItem : grnItems) {
            GoodsReceiptResponseDto.Item item = new GoodsReceiptResponseDto.Item();
            item.productId = grnItem.getProduct().getId();
            item.productName = grnItem.getProduct().getProductName();
            item.quantityReceived = grnItem.getQuantityReceived();
            item.unitCost = grnItem.getUnitCost();
            item.lineTotal = grnItem.getUnitCost().multiply(BigDecimal.valueOf(grnItem.getQuantityReceived()));
            total = total.add(item.lineTotal);
            items.add(item);
        }
        dto.items = items;
        dto.totalReceivedAmount = total;
        return dto;
    }

    private void validateCreatePo(PurchaseOrderCreateRequestDto dto) {
        if (dto == null) throw new IllegalArgumentException("Request body is required");
        if (dto.supplierId == null) throw new IllegalArgumentException("supplierId is required");
        if (dto.items == null || dto.items.isEmpty()) throw new IllegalArgumentException("At least one item is required");
        for (PurchaseOrderCreateRequestDto.Item item : dto.items) {
            if (item == null) throw new IllegalArgumentException("Item is required");
            if (item.productId == null) throw new IllegalArgumentException("productId is required");
            if (item.quantity == null || item.quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
            if (item.unitPrice != null && item.unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("unitPrice must be > 0");
            }
        }
    }

    private void validateReceiveItem(GoodsReceiptRequestDto.Item item) {
        if (item == null) throw new IllegalArgumentException("Item is required");
        if (item.productId == null) throw new IllegalArgumentException("productId is required");
        if (item.quantityReceived == null || item.quantityReceived <= 0) {
            throw new IllegalArgumentException("quantityReceived must be > 0");
        }
        if (item.unitCost == null || item.unitCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitCost must be > 0");
        }
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("expectedDeliveryDate must be yyyy-MM-dd");
        }
    }
}
