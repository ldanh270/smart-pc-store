package dto.purchase;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for goods receipt note details.
 */
public class GoodsReceiptResponseDto {

    public Integer id;
    public Integer poId;
    public String receiptDate;
    public String note;
    public List<Item> items;
    public BigDecimal totalReceivedAmount;

    /**
     * GRN line item output.
     */
    public static class Item {

        public Integer productId;
        public String productName;
        public Integer quantityReceived;
        public BigDecimal unitCost;
        public BigDecimal lineTotal;
    }
}
