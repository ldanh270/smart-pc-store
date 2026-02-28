package services;

import dto.product.ProductResponseDto;
import entities.Category;
import entities.Product;
import entities.Supplier;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class ProductServiceToDtoTest {

    @Test
    public void testStockStatusMapping() {
        ProductService svc = new ProductService(null);

        Supplier s = new Supplier();
        s.setId(1);
        s.setSupplierName("Acme");

        Category c = new Category();
        c.setId(2);
        c.setCategoryName("Components");

        Product p0 = new Product();
        p0.setId(10);
        p0.setProductName("P0");
        p0.setCurrentPrice(new BigDecimal("10.00"));
        p0.setQuantity(0);
        p0.setSupplier(s);
        p0.setCategory(c);

        ProductResponseDto d0 = svc.toDto(p0);
        Assert.assertEquals("Out of stock", d0.stockStatus);

        Product p1 = new Product();
        p1.setId(11);
        p1.setProductName("P1");
        p1.setCurrentPrice(new BigDecimal("15.00"));
        p1.setQuantity(1);
        p1.setSupplier(s);
        p1.setCategory(c);

        ProductResponseDto d1 = svc.toDto(p1);
        Assert.assertEquals("Low stock", d1.stockStatus);

        Product p5 = new Product();
        p5.setId(12);
        p5.setProductName("P5");
        p5.setCurrentPrice(new BigDecimal("20.00"));
        p5.setQuantity(5);
        p5.setSupplier(s);
        p5.setCategory(c);

        ProductResponseDto d5 = svc.toDto(p5);
        Assert.assertEquals("Low stock", d5.stockStatus);

        Product p6 = new Product();
        p6.setId(13);
        p6.setProductName("P6");
        p6.setCurrentPrice(new BigDecimal("25.00"));
        p6.setQuantity(6);
        p6.setSupplier(s);
        p6.setCategory(c);

        ProductResponseDto d6 = svc.toDto(p6);
        Assert.assertEquals("In stock", d6.stockStatus);
    }
}
