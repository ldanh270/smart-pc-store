package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dao.SupplierPriceHistoryDao;
import dto.supplieranalytics.PriceCompareDto;
import dto.supplieranalytics.PriceTrendResponseDto;
import entities.SupplierPriceHistory;

/**
 * Service class for supplier price analytics. Provides latest-price comparison
 * and trend analysis.
 */
public class SupplierAnalyticsService {

    private final SupplierPriceHistoryDao supplierPriceHistoryDao;

    /**
     * Constructor.
     *
     * @param supplierPriceHistoryDao Supplier price history DAO.
     */
    public SupplierAnalyticsService(SupplierPriceHistoryDao supplierPriceHistoryDao) {
        this.supplierPriceHistoryDao = supplierPriceHistoryDao;
    }

    /**
     * Compare latest import prices across suppliers for one product.
     *
     * @param productId Product ID.
     * @return Price comparison list sorted by ascending price.
     */
    public List<PriceCompareDto> compareLatestPrices(Integer productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        return supplierPriceHistoryDao.findLatestByProduct(productId).stream().map(this::toCompareDto).collect(
                Collectors.toList());
    }

    /**
     * Analyze quotation trend for one supplier and one product.
     *
     * @param productId  Product ID.
     * @param supplierId Supplier ID.
     * @return Trend response DTO or null if no history exists.
     */
    public PriceTrendResponseDto getPriceTrend(Integer productId, Integer supplierId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (supplierId == null) {
            throw new IllegalArgumentException("supplierId is required");
        }

        List<SupplierPriceHistory> points = supplierPriceHistoryDao.findByProductAndSupplier(productId, supplierId);
        if (points.isEmpty()) {
            return null;
        }
        if (points.stream().anyMatch(p -> p.getImportPrice() == null)) {
            throw new IllegalArgumentException("Price history data is incomplete");
        }

        SupplierPriceHistory first = points.get(0);
        SupplierPriceHistory last = points.get(points.size() - 1);
        BigDecimal firstPrice = first.getImportPrice();
        BigDecimal lastPrice = last.getImportPrice();

        BigDecimal percent;
        if (firstPrice.compareTo(BigDecimal.ZERO) == 0) {
            percent = BigDecimal.ZERO;
        } else {
            percent = lastPrice.subtract(firstPrice).multiply(BigDecimal.valueOf(100)).divide(
                    firstPrice,
                    2,
                    RoundingMode.HALF_UP
            );
        }

        PriceTrendResponseDto dto = new PriceTrendResponseDto();
        dto.supplierId = supplierId;
        dto.supplierName = first.getSupplier().getSupplierName();
        dto.productId = productId;
        dto.productName = first.getProduct().getProductName();
        dto.firstPrice = firstPrice;
        dto.lastPrice = lastPrice;
        dto.percentageChange = percent;
        dto.trend = trendLabel(percent);
        dto.points = new ArrayList<>();

        for (SupplierPriceHistory point : points) {
            PriceTrendResponseDto.Point p = new PriceTrendResponseDto.Point();
            p.effectiveDate = point.getEffectiveDate() == null ? null : point.getEffectiveDate().toString();
            p.importPrice = point.getImportPrice();
            dto.points.add(p);
        }
        return dto;
    }

    private PriceCompareDto toCompareDto(SupplierPriceHistory history) {
        PriceCompareDto dto = new PriceCompareDto();
        dto.supplierId = history.getSupplier().getId();
        dto.supplierName = history.getSupplier().getSupplierName();
        dto.productId = history.getProduct().getId();
        dto.productName = history.getProduct().getProductName();
        dto.importPrice = history.getImportPrice();
        dto.effectiveDate = history.getEffectiveDate() == null ? null : history.getEffectiveDate().toString();
        return dto;
    }

    private String trendLabel(BigDecimal percent) {
        if (percent.compareTo(BigDecimal.valueOf(3)) >= 0) {
            return "UP";
        }
        if (percent.compareTo(BigDecimal.valueOf(-3)) <= 0) {
            return "DOWN";
        }
        return "STABLE";
    }
}
