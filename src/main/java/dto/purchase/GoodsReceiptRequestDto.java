package dto.purchase;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for receiving goods against a purchase order.
 */
public class GoodsReceiptRequestDto {
    public String note;
    public List<Item> items;

    /**
     * GRN line item input.
     */
    public static class Item {
        public Integer productId;
        public Integer quantityReceived;
        public BigDecimal unitCost;
    }
}
