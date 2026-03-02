package services;

import dto.supplier.SupplierRequestDto;
import entities.Supplier;
import org.junit.Assert;
import org.junit.Test;

public class SupplierServiceValidationTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullRequest() {
        SupplierService svc = new SupplierService(null, null);
        try {
            svc.create(null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Request body is required", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithBlankSupplierName() {
        SupplierService svc = new SupplierService(null, null);
        SupplierRequestDto dto = new SupplierRequestDto();
        dto.supplierName = "   ";
        dto.leadTimeDays = 3;

        try {
            svc.create(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Supplier name is required", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNegativeLeadTime() {
        SupplierService svc = new SupplierService(null, null);
        SupplierRequestDto dto = new SupplierRequestDto();
        dto.supplierName = "ABC";
        dto.leadTimeDays = -1;

        try {
            svc.create(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Lead time must be >= 0", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testToDtoMapping() {
        SupplierService svc = new SupplierService(null, null);
        Supplier supplier = new Supplier();
        supplier.setId(1);
        supplier.setSupplierName("ABC Supplier");
        supplier.setContactInfo("abc@example.com");
        supplier.setComponentTypes("CPU,GPU");
        supplier.setLeadTimeDays(5);
        supplier.setStatus(true);

        var dto = svc.toDto(supplier);
        Assert.assertEquals(Integer.valueOf(1), dto.id);
        Assert.assertEquals("ABC Supplier", dto.supplierName);
        Assert.assertEquals("abc@example.com", dto.contactInfo);
        Assert.assertEquals("CPU,GPU", dto.componentTypes);
        Assert.assertEquals(Integer.valueOf(5), dto.leadTimeDays);
        Assert.assertEquals(true, dto.status);
    }
}
