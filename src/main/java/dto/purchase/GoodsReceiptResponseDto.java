package dto.purchase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for goods receipt note details.
 */
public class GoodsReceiptResponseDto {

    public UUID id;
    public UUID poId;
    public String receiptDate;
    public String note;
    public List<Item> items;
    public BigDecimal totalReceivedAmount;

    /**
     * GRN line item output.
     */
    public static class Item {

        public UUID productId;
        public String productName;
        public Integer quantityReceived;
        public BigDecimal unitCost;
        public BigDecimal lineTotal;
    }
}
