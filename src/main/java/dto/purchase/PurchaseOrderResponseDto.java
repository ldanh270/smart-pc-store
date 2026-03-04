package dto.purchase;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for purchase order details.
 */
public class PurchaseOrderResponseDto {

    public Integer id;
    public String poCode;
    public Integer supplierId;
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

        public Integer id;
        public Integer productId;
        public String productName;
        public Integer quantity;
        public BigDecimal unitPrice;
        public BigDecimal lineTotal;
    }
}
