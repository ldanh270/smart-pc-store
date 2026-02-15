package services;

import dto.product.ProductRequestDto;
import entities.Category;
import entities.Product;
import entities.Supplier;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Test ProductService create/update validations.
 */
public class ProductServiceValidationTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullProductName() {
        ProductService svc = new ProductService(null, null);
        ProductRequestDto dto = new ProductRequestDto();
        dto.productName = null;
        dto.currentPrice = new BigDecimal("10.00");
        dto.quantity = 5;
        dto.supplierId = 1;
        dto.categoryId = 1;

        try {
            svc.create(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Product name is required", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithBlankProductName() {
        ProductService svc = new ProductService(null, null);
        ProductRequestDto dto = new ProductRequestDto();
        dto.productName = "   ";
        dto.currentPrice = new BigDecimal("10.00");
        dto.quantity = 5;
        dto.supplierId = 1;
        dto.categoryId = 1;

        try {
            svc.create(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Product name is required", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithInvalidPrice() {
        ProductService svc = new ProductService(null, null);
        ProductRequestDto dto = new ProductRequestDto();
        dto.productName = "Test Product";
        dto.currentPrice = BigDecimal.ZERO;
        dto.quantity = 5;
        dto.supplierId = 1;
        dto.categoryId = 1;

        try {
            svc.create(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Price must be greater than 0", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNegativeQuantity() {
        ProductService svc = new ProductService(null, null);
        ProductRequestDto dto = new ProductRequestDto();
        dto.productName = "Test Product";
        dto.currentPrice = new BigDecimal("10.00");
        dto.quantity = -1;
        dto.supplierId = 1;
        dto.categoryId = 1;

        try {
            svc.create(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Quantity must be >= 0", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullSupplierId() {
        ProductService svc = new ProductService(null, null);
        ProductRequestDto dto = new ProductRequestDto();
        dto.productName = "Test Product";
        dto.currentPrice = new BigDecimal("10.00");
        dto.quantity = 5;
        dto.supplierId = null;
        dto.categoryId = 1;

        try {
            svc.create(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Supplier is required", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullCategoryId() {
        ProductService svc = new ProductService(null, null);
        ProductRequestDto dto = new ProductRequestDto();
        dto.productName = "Test Product";
        dto.currentPrice = new BigDecimal("10.00");
        dto.quantity = 5;
        dto.supplierId = 1;
        dto.categoryId = null;

        try {
            svc.create(dto);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Category is required", e.getMessage());
            throw e;
        }
    }

    /**
     * Test stock status thresholds precisely.
     */
    @Test
    public void testStockStatusEdgeCases() {
        ProductService svc = new ProductService(null, null);

        Supplier s = new Supplier();
        s.setId(1);
        s.setSupplierName("Test Supplier");

        Category c = new Category();
        c.setId(1);
        c.setCategoryName("Test Category");

        // Quantity = 0: Out of stock
        Product p0 = new Product();
        p0.setId(1);
        p0.setProductName("P0");
        p0.setCurrentPrice(new BigDecimal("10.00"));
        p0.setQuantity(0);
        p0.setSupplier(s);
        p0.setCategory(c);
        Assert.assertEquals("Out of stock", svc.toDto(p0).stockStatus);

        // Quantity = 1: Low stock
        Product p1 = new Product();
        p1.setId(2);
        p1.setProductName("P1");
        p1.setCurrentPrice(new BigDecimal("10.00"));
        p1.setQuantity(1);
        p1.setSupplier(s);
        p1.setCategory(c);
        Assert.assertEquals("Low stock", svc.toDto(p1).stockStatus);

        // Quantity = 5: Low stock (boundary)
        Product p5 = new Product();
        p5.setId(3);
        p5.setProductName("P5");
        p5.setCurrentPrice(new BigDecimal("10.00"));
        p5.setQuantity(5);
        p5.setSupplier(s);
        p5.setCategory(c);
        Assert.assertEquals("Low stock", svc.toDto(p5).stockStatus);

        // Quantity = 6: In stock
        Product p6 = new Product();
        p6.setId(4);
        p6.setProductName("P6");
        p6.setCurrentPrice(new BigDecimal("10.00"));
        p6.setQuantity(6);
        p6.setSupplier(s);
        p6.setCategory(c);
        Assert.assertEquals("In stock", svc.toDto(p6).stockStatus);

        // Quantity = null: Out of stock
        Product pNull = new Product();
        pNull.setId(5);
        pNull.setProductName("PNull");
        pNull.setCurrentPrice(new BigDecimal("10.00"));
        pNull.setQuantity(null);
        pNull.setSupplier(s);
        pNull.setCategory(c);
        Assert.assertEquals("Out of stock", svc.toDto(pNull).stockStatus);
    }

    /**
     * Test DTO field mapping.
     */
    @Test
    public void testDtoFieldMapping() {
        ProductService svc = new ProductService(null, null);

        Supplier s = new Supplier();
        s.setId(100);
        s.setSupplierName("Supplier ABC");

        Category c = new Category();
        c.setId(200);
        c.setCategoryName("Category XYZ");

        Product p = new Product();
        p.setId(10);
        p.setProductName("Product Test");
        p.setDescription("Test Description");
        p.setImageUrl("http://example.com/image.jpg");
        p.setCurrentPrice(new BigDecimal("99.99"));
        p.setQuantity(8);
        p.setStatus(true);
        p.setSupplier(s);
        p.setCategory(c);

        var dto = svc.toDto(p);

        Assert.assertEquals(Integer.valueOf(10), dto.id);
        Assert.assertEquals("Product Test", dto.productName);
        Assert.assertEquals("Test Description", dto.description);
        Assert.assertEquals("http://example.com/image.jpg", dto.imageUrl);
        Assert.assertEquals(new BigDecimal("99.99"), dto.currentPrice);
        Assert.assertEquals(Integer.valueOf(8), dto.quantity);
        Assert.assertEquals(true, dto.status);
        Assert.assertEquals(Integer.valueOf(100), dto.supplierId);
        Assert.assertEquals("Supplier ABC", dto.supplierName);
        Assert.assertEquals(Integer.valueOf(200), dto.categoryId);
        Assert.assertEquals("Category XYZ", dto.categoryName);
        Assert.assertEquals("In stock", dto.stockStatus);
    }
}
