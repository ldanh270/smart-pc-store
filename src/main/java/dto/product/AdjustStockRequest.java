package dto.product;

/**
 * Data Transfer Object for stock adjustment requests.
 * Contains the delta quantity to adjust product stock.
 * Positive delta increases stock, negative delta decreases stock.
 */
public class AdjustStockRequest {
    public Integer delta;
}
