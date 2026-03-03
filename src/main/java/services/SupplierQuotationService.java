package services;

import dao.SupplierPriceHistoryDao;
import dao.JPAUtil;
import dto.supplierquotation.SupplierQuotationRequestDto;
import dto.supplierquotation.SupplierQuotationResponseDto;
import entities.Product;
import entities.Supplier;
import entities.SupplierPriceHistory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for supplier quotations and price history.
 * Supports creating quotations and querying historical price records.
 */
public class SupplierQuotationService {

    private final SupplierPriceHistoryDao priceHistoryDao;

    /**
     * Constructor.
     *
     * @param priceHistoryDao Supplier price history DAO.
     */
    public SupplierQuotationService(SupplierPriceHistoryDao priceHistoryDao) {
        this.priceHistoryDao = priceHistoryDao;
    }

    /**
     * Create one quotation record (append-only history).
     *
     * @param dto Quotation request.
     * @return Created quotation DTO.
     */
    public SupplierQuotationResponseDto create(SupplierQuotationRequestDto dto) {
        validate(dto);
        Supplier supplier = JPAUtil.getEntityManager().find(Supplier.class, dto.supplierId);
        if (supplier == null) throw new IllegalArgumentException("Supplier not found");
        Product product = JPAUtil.getEntityManager().find(Product.class, dto.productId);
        if (product == null) throw new IllegalArgumentException("Product not found");

        SupplierPriceHistory history = new SupplierPriceHistory();
        history.setSupplier(supplier);
        history.setProduct(product);
        history.setImportPrice(dto.importPrice);
        history.setEffectiveDate(parseDateOrToday(dto.effectiveDate));

        try {
            JPAUtil.getEntityManager().getTransaction().begin();
            priceHistoryDao.create(history);
            JPAUtil.getEntityManager().getTransaction().commit();
            return toDto(history);
        } catch (Exception e) {
            if (JPAUtil.getEntityManager().getTransaction().isActive()) JPAUtil.getEntityManager().getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Retrieve quotation history for one product from one supplier.
     *
     * @param productId Product ID.
     * @param supplierId Supplier ID.
     * @return Quotation history DTO list.
     */
    public List<SupplierQuotationResponseDto> getHistory(Integer productId, Integer supplierId) {
        if (productId == null || supplierId == null) {
            throw new IllegalArgumentException("productId and supplierId are required");
        }
        return priceHistoryDao.findByProductAndSupplier(productId, supplierId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert SupplierPriceHistory entity to response DTO.
     *
     * @param history Quotation entity.
     * @return Quotation response DTO.
     */
    public SupplierQuotationResponseDto toDto(SupplierPriceHistory history) {
        SupplierQuotationResponseDto dto = new SupplierQuotationResponseDto();
        dto.id = history.getId();
        dto.supplierId = history.getSupplier().getId();
        dto.supplierName = history.getSupplier().getSupplierName();
        dto.productId = history.getProduct().getId();
        dto.productName = history.getProduct().getProductName();
        dto.importPrice = history.getImportPrice();
        dto.effectiveDate = history.getEffectiveDate() == null ? null : history.getEffectiveDate().toString();
        return dto;
    }

    private void validate(SupplierQuotationRequestDto dto) {
        if (dto == null) throw new IllegalArgumentException("Request body is required");
        if (dto.supplierId == null) throw new IllegalArgumentException("supplierId is required");
        if (dto.productId == null) throw new IllegalArgumentException("productId is required");
        if (dto.importPrice == null || dto.importPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("importPrice must be > 0");
        }
    }

    private LocalDate parseDateOrToday(String s) {
        if (s == null || s.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("effectiveDate must be yyyy-MM-dd");
        }
    }
}
