package dto.purchase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

        public UUID productId;
        public Integer quantityReceived;
        public BigDecimal unitCost;
    }
}
