package dto.purchase;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a purchase order.
 */
public class PurchaseOrderCreateRequestDto {

    public Integer supplierId;
    public String expectedDeliveryDate;
    public String note;
    public List<Item> items;

    /**
     * PO line item input.
     */
    public static class Item {

        public Integer productId;
        public Integer quantity;
        public BigDecimal unitPrice;
    }
}
