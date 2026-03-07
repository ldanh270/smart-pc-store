package dto.purchase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for purchase order details.
 */
public class PurchaseOrderResponseDto {

    public UUID id;
    public String poCode;
    public UUID supplierId;
    public String supplierName;
    public String orderDate;
    public String expectedDeliveryDate;
    public String status;
    public List<Item> items;
    public BigDecimal totalAmount;

    /**
     * PO line item output.
     */
    public static class Item {

        public UUID id;
        public UUID productId;
        public String productName;
        public Integer quantity;
        public BigDecimal unitPrice;
        public BigDecimal lineTotal;
    }
}
