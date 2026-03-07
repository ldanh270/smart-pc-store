package dto.purchase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a purchase order.
 */
public class PurchaseOrderCreateRequestDto {

    public UUID supplierId;
    public String expectedDeliveryDate;
    public String note;
    public List<Item> items;

    /**
     * PO line item input.
     */
    public static class Item {

        public UUID productId;
        public Integer quantity;
        public BigDecimal unitPrice;
    }
}
