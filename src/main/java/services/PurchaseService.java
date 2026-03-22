package services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dao.InventoryTransactionDao;
import dao.JPAUtil;
import dao.ProductDao;
import dao.PurchaseOrderDao;
import dao.PurchaseOrderItemDao;
import dao.SupplierDao;
import dto.purchase.PurchaseOrderCreateRequestDto;
import dto.purchase.PurchaseOrderResponseDto;
import entities.InventoryTransaction;
import entities.Product;
import entities.PurchaseOrder;
import entities.PurchaseOrderItem;
import entities.Supplier;

/**
 * Service class for purchasing workflow. Handles PO creation, stock updates,
 * and PO status transitions.
 */
public class PurchaseService {

    private final PurchaseOrderDao purchaseOrderDao;
    private final PurchaseOrderItemDao purchaseOrderItemDao;
    private final SupplierDao supplierDao;
    private final ProductDao productDao;
    private final InventoryTransactionDao inventoryTransactionDao;

    /**
     * Constructor
     */
    public PurchaseService(PurchaseOrderDao purchaseOrderDao, PurchaseOrderItemDao purchaseOrderItemDao, SupplierDao supplierDao, ProductDao productDao, InventoryTransactionDao inventoryTransactionDao) {
        this.purchaseOrderDao = purchaseOrderDao;
        this.purchaseOrderItemDao = purchaseOrderItemDao;
        this.supplierDao = supplierDao;
        this.productDao = productDao;
        this.inventoryTransactionDao = inventoryTransactionDao;
    }

    /**
     * Create a purchase order with multiple items. Uses provided unit price or
     * latest purchase price when unit price is missing.
     *
     * @param dto Purchase order request.
     * @return Created purchase order response.
     */
    public PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderCreateRequestDto dto) {
        validateCreatePo(dto);
        Supplier supplier = resolveSupplier(dto);

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setOrderDate(LocalDate.now());
        po.setExpectedDeliveryDate(dto.expectedDeliveryDate);
        po.setNote(dto.note);

        List<PurchaseOrderItem> poItems = new ArrayList<>();

        try {
            JPAUtil.getEntityManager().getTransaction().begin();
            purchaseOrderDao.create(po);

            for (PurchaseOrderCreateRequestDto.Item itemDto : dto.items) {
                Product product = productDao.findById(itemDto.productId);
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + itemDto.productId);
                }
                if (itemDto.quantity == null || itemDto.quantity <= 0) {
                    throw new IllegalArgumentException("Quantity must be > 0");
                }

                if (itemDto.unitPrice != null && itemDto.unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("unitPrice must be > 0");
                }

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPo(po);
                item.setProduct(product);
                item.setQuantity(itemDto.quantity);
                item.setUnitPrice(itemDto.unitPrice);
                purchaseOrderItemDao.create(item);
                poItems.add(item);

                // Update product stock and price
                int currentQty = product.getQuantity() == null ? 0 : product.getQuantity();
                product.setQuantity(currentQty + itemDto.quantity);
                if (itemDto.unitPrice != null) {
                    product.setCurrentPrice(itemDto.unitPrice);
                }
                productDao.update(product);

                // Record inventory transaction
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProduct(product);
                transaction.setQuantityChange(itemDto.quantity);
                transaction.setTransactionType("PO_RECEIPT");
                transaction.setTransactionDate(java.time.OffsetDateTime.now());
                inventoryTransactionDao.create(transaction);
            }

            JPAUtil.getEntityManager().getTransaction().commit();
            return toPoDto(po, poItems);
        } catch (IllegalArgumentException e) {
            if (JPAUtil.getEntityManager().getTransaction().isActive()) {
                JPAUtil.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Update a purchase order and ensure data integrity.
     *
     * @param id  Purchase order ID.
     * @param dto Purchase order request.
     * @return Updated purchase order response.
     */
    public PurchaseOrderResponseDto updatePurchaseOrder(UUID id, PurchaseOrderCreateRequestDto dto) {
        validateCreatePo(dto);
        if (id == null) {
            throw new IllegalArgumentException("PO id is required");
        }
        Supplier supplier = resolveSupplier(dto);

        try {
            JPAUtil.getEntityManager().getTransaction().begin();

            PurchaseOrder po = purchaseOrderDao.findById(id);
            if (po == null) {
                throw new IllegalArgumentException("Purchase order not found");
            }

            po.setSupplier(supplier);
            po.setExpectedDeliveryDate(dto.expectedDeliveryDate);
            po.setNote(dto.note);
            purchaseOrderDao.update(po);

            List<PurchaseOrderItem> oldItems = purchaseOrderItemDao.findByPurchaseOrderId(po.getId());
            Map<UUID, Integer> productQtyDiff = new HashMap<>();

            // Revert old items
            for (PurchaseOrderItem oldItem : oldItems) {
                UUID pId = oldItem.getProduct().getId();
                productQtyDiff.put(pId, productQtyDiff.getOrDefault(pId, 0) - oldItem.getQuantity());
                purchaseOrderItemDao.delete(oldItem.getId());
            }

            // Apply new items
            List<PurchaseOrderItem> newPoItems = new ArrayList<>();
            for (PurchaseOrderCreateRequestDto.Item itemDto : dto.items) {
                Product product = productDao.findById(itemDto.productId);
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + itemDto.productId);
                }
                if (itemDto.quantity == null || itemDto.quantity <= 0) {
                    throw new IllegalArgumentException("Quantity must be > 0");
                }
                if (itemDto.unitPrice != null && itemDto.unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("unitPrice must be > 0");
                }

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPo(po);
                item.setProduct(product);
                item.setQuantity(itemDto.quantity);
                item.setUnitPrice(itemDto.unitPrice);
                purchaseOrderItemDao.create(item);
                newPoItems.add(item);

                UUID pId = product.getId();
                productQtyDiff.put(pId, productQtyDiff.getOrDefault(pId, 0) + itemDto.quantity);

                // Update price if provided
                if (itemDto.unitPrice != null) {
                    product.setCurrentPrice(itemDto.unitPrice);
                }
            }

            // Update product stock and record transactions
            for (Map.Entry<UUID, Integer> entry : productQtyDiff.entrySet()) {
                UUID pId = entry.getKey();
                int diff = entry.getValue();

                Product product = productDao.findById(pId);
                int currentQty = product.getQuantity() == null ? 0 : product.getQuantity();
                
                if (currentQty + diff < 0) {
                    throw new IllegalArgumentException("Insufficient stock to update PO for product " + pId);
                }

                product.setQuantity(currentQty + diff);
                productDao.update(product);

                if (diff != 0) {
                    InventoryTransaction transaction = new InventoryTransaction();
                    transaction.setProduct(product);
                    transaction.setQuantityChange(diff);
                    transaction.setTransactionType("PO_UPDATE");
                    transaction.setTransactionDate(java.time.OffsetDateTime.now());
                    inventoryTransactionDao.create(transaction);
                }
            }

            JPAUtil.getEntityManager().getTransaction().commit();
            return toPoDto(po, newPoItems);
        } catch (IllegalArgumentException e) {
            if (JPAUtil.getEntityManager().getTransaction().isActive()) {
                JPAUtil.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Retrieve one purchase order with line items.
     *
     * @param poId Purchase order ID.
     * @return Purchase order response or null if not found.
     */
    public PurchaseOrderResponseDto getPurchaseOrder(UUID poId) {
        PurchaseOrder po = purchaseOrderDao.findById(poId);
        if (po == null) {
            return null;
        }
        List<PurchaseOrderItem> items = purchaseOrderItemDao.findByPurchaseOrderId(poId);
        return toPoDto(po, items);
    }

    /**
     * Retrieve all purchase orders with line items.
     *
     * @return List of Purchase order responses.
     */
    public List<PurchaseOrderResponseDto> getAllPurchaseOrders(String query, Integer page, Integer size) {
        List<PurchaseOrder> pos = purchaseOrderDao.searchAndPaginate(query, page, size);
        List<PurchaseOrderResponseDto> dtos = new ArrayList<>();
        for (PurchaseOrder po : pos) {
            List<PurchaseOrderItem> items = purchaseOrderItemDao.findByPurchaseOrderId(po.getId());
            PurchaseOrderResponseDto dto = toPoDto(po, items);
            dto.items = null;
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Create an adjustment order (supports negative quantity).
     *
     * @param parentId Parent PO ID.
     * @param dto      Adjustment order request.
     * @return Created adjustment order response.
     */
    public PurchaseOrderResponseDto createAdjustmentOrder(UUID parentId, PurchaseOrderCreateRequestDto dto) {
        if (parentId == null) {
            throw new IllegalArgumentException("Parent PO id is required");
        }
        
        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.items == null || dto.items.isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }
        for (PurchaseOrderCreateRequestDto.Item item : dto.items) {
            if (item == null) {
                throw new IllegalArgumentException("Item is required");
            }
            if (item.productId == null) {
                throw new IllegalArgumentException("productId is required");
            }
            if (item.quantity == null || item.quantity == 0) {
                throw new IllegalArgumentException("Quantity must be non-zero");
            }
        }

        try {
            JPAUtil.getEntityManager().getTransaction().begin();

            PurchaseOrder parent = purchaseOrderDao.findById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("Parent purchase order not found");
            }

            PurchaseOrder adjustment = new PurchaseOrder();
            adjustment.setSupplier(parent.getSupplier());
            adjustment.setOrderDate(LocalDate.now());
            adjustment.setType("ADJUSTMENT");
            adjustment.setParentOrder(parent);
            purchaseOrderDao.create(adjustment);

            List<PurchaseOrderItem> poiItems = new ArrayList<>();
            for (PurchaseOrderCreateRequestDto.Item itemDto : dto.items) {
                Product product = productDao.findById(itemDto.productId);
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + itemDto.productId);
                }

                int currentQty = product.getQuantity() == null ? 0 : product.getQuantity();
                if (currentQty + itemDto.quantity < 0) {
                    // Cannot adjust due to insufficient stock
                    throw new IllegalStateException("Insufficient stock to perform adjustment. Product: " + product.getProductName() + 
                        " (在庫が不足しているため、調整できません。製品: " + product.getProductName() + ")");
                }

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPo(adjustment);
                item.setProduct(product);
                item.setQuantity(itemDto.quantity);
                item.setUnitPrice(itemDto.unitPrice);
                purchaseOrderItemDao.create(item);
                poiItems.add(item);

                // Update stock
                product.setQuantity(currentQty + itemDto.quantity);
                if (itemDto.unitPrice != null) {
                    product.setCurrentPrice(itemDto.unitPrice);
                }
                productDao.update(product);

                // Record transaction
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProduct(product);
                transaction.setQuantityChange(itemDto.quantity);
                transaction.setTransactionType("PO_ADJUSTMENT");
                transaction.setTransactionDate(java.time.OffsetDateTime.now());
                inventoryTransactionDao.create(transaction);
            }

            JPAUtil.getEntityManager().getTransaction().commit();
            return toPoDto(adjustment, poiItems);
        } catch (Exception e) {
            if (JPAUtil.getEntityManager().getTransaction().isActive()) {
                JPAUtil.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Get adjusted quantity of a product in a PO family.
     *
     * @param poId      PO ID.
     * @param productId Product ID.
     * @return Total adjusted quantity.
     */
    public int getAdjustedQuantity(UUID poId, UUID productId) {
        List<PurchaseOrderItem> familyItems = purchaseOrderItemDao.findByProductAndOrderFamily(productId, poId);
        int total = 0;
        for (PurchaseOrderItem item : familyItems) {
            total += (item.getQuantity() != null ? item.getQuantity() : 0);
        }
        return total;
    }

    private PurchaseOrderResponseDto toPoDto(PurchaseOrder po, List<PurchaseOrderItem> poItems) {
        PurchaseOrderResponseDto dto = new PurchaseOrderResponseDto();
        dto.id = po.getId();
        dto.supplierId = po.getSupplier() == null ? null : po.getSupplier().getId();
        dto.supplierName = po.getSupplier() == null ? null : po.getSupplier().getSupplierName();
        dto.orderDate = po.getOrderDate() == null ? null : po.getOrderDate().toString();
        dto.expectedDeliveryDate = po.getExpectedDeliveryDate() == null ? null : po.getExpectedDeliveryDate().toString();
        dto.note = po.getNote();
        dto.type = po.getType();

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseOrderResponseDto.Item> items = new ArrayList<>();
        for (PurchaseOrderItem poItem : poItems) {
            PurchaseOrderResponseDto.Item item = new PurchaseOrderResponseDto.Item();
            item.id = poItem.getId();
            item.productId = poItem.getProduct() == null ? null : poItem.getProduct().getId();
            item.productName = poItem.getProduct() == null ? null : poItem.getProduct().getProductName();
            item.quantity = poItem.getQuantity();
            item.unitPrice = poItem.getUnitPrice();
            BigDecimal unitPrice = poItem.getUnitPrice() == null ? BigDecimal.ZERO : poItem.getUnitPrice();
            int quantity = poItem.getQuantity() == null ? 0 : poItem.getQuantity();
            item.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            total = total.add(item.lineTotal);
            items.add(item);
        }
        dto.items = items;
        dto.totalAmount = total;
        return dto;
    }


    private void validateCreatePo(PurchaseOrderCreateRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.supplierName != null) {
            dto.supplierName = dto.supplierName.trim();
            if (dto.supplierName.isEmpty()) {
                dto.supplierName = null;
            }
        }
        if (dto.supplierId == null && dto.supplierName == null) {
            throw new IllegalArgumentException("supplierId or supplierName is required");
        }
        if (dto.items == null || dto.items.isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }
        for (PurchaseOrderCreateRequestDto.Item item : dto.items) {
            if (item == null) {
                throw new IllegalArgumentException("Item is required");
            }
            if (item.productId == null) {
                throw new IllegalArgumentException("productId is required");
            }
            if (item.quantity == null || item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0");
            }
            if (item.unitPrice != null && item.unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("unitPrice must be > 0");
            }
        }
    }

    private Supplier resolveSupplier(PurchaseOrderCreateRequestDto dto) {
        Supplier byId = null;
        if (dto.supplierId != null) {
            byId = supplierDao.findById(dto.supplierId);
            if (byId == null) {
                throw new IllegalArgumentException("Supplier not found");
            }
        }

        if (dto.supplierName != null) {
            List<Supplier> matches = supplierDao.findActiveByExactNameIgnoreCase(dto.supplierName);
            if (matches.isEmpty()) {
                throw new IllegalArgumentException("Supplier not found");
            }
            if (matches.size() > 1) {
                throw new IllegalArgumentException("Multiple suppliers found for supplierName");
            }

            Supplier byName = matches.get(0);
            if (byId != null && !byId.getId().equals(byName.getId())) {
                throw new IllegalArgumentException("supplierId and supplierName do not match");
            }
            return byName;
        }

        return byId;
    }
}
