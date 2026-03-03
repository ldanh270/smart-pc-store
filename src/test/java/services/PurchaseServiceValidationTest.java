package services;

import dto.purchase.PurchaseOrderCreateRequestDto;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PurchaseServiceValidationTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePoWithNullRequest() {
        PurchaseService svc = new PurchaseService(null, null, null, null, null, null, null, null);
        try {
            svc.createPurchaseOrder(null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Request body is required", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePoWithMissingProductId() {
        PurchaseService svc = new PurchaseService(null, null, null, null, null, null, null, null);

        PurchaseOrderCreateRequestDto dto = new PurchaseOrderCreateRequestDto();
        dto.supplierId = 1;
        dto.items = new ArrayList<>();

        PurchaseOrderCreateRequestDto.Item item = new PurchaseOrderCreateRequestDto.Item();
        item.productId = null;
        item.quantity = 1;
        item.unitPrice = new BigDecimal("10.00");
        dto.items.add(item);

        try {
            svc.createPurchaseOrder(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("productId is required", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePoWithInvalidUnitPrice() {
        PurchaseService svc = new PurchaseService(null, null, null, null, null, null, null, null);

        PurchaseOrderCreateRequestDto dto = new PurchaseOrderCreateRequestDto();
        dto.supplierId = 1;
        dto.items = new ArrayList<>();

        PurchaseOrderCreateRequestDto.Item item = new PurchaseOrderCreateRequestDto.Item();
        item.productId = 10;
        item.quantity = 1;
        item.unitPrice = BigDecimal.ZERO;
        dto.items.add(item);

        try {
            svc.createPurchaseOrder(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("unitPrice must be > 0", e.getMessage());
            throw e;
        }
    }
}
