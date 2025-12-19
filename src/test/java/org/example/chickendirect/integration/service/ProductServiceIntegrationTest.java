package org.example.chickendirect.integration.service;

import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.ProductRepo;
import org.example.chickendirect.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


public class ProductServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepo productRepo;

    @BeforeEach
    void setUp() {
        productRepo.deleteAll();
    }

    @Test
    void testCreateProduct_success() {
        ProductDto dto = new ProductDto("Chicken Wings", "Tasty wings", BigDecimal.valueOf(10), 20, "kg");
        Product result = productService.createProduct(dto);

        assertNotNull(result.getProductId());
        assertEquals(ProductStatus.IN_STOCK, result.getProductStatus());
    }

    @Test
    void testFindProductById_found() {
        Product product = new Product();
        product.setName("Chicken Wings");
        product.setPrice(BigDecimal.TEN);
        product.setQuantity(10);
        product.setUnit("kg");
        product.setProductStatus(ProductStatus.IN_STOCK);
        product = productRepo.save(product);

        Product found = productService.findProductById(product.getProductId());
        assertEquals(product.getProductId(), found.getProductId());
    }

    @Test
    void testUpdateProductPrice() {
        Product product = new Product();
        product.setName("Chicken Wings");
        product.setPrice(BigDecimal.valueOf(10));
        product.setQuantity(10);
        product.setUnit("kg");
        product.setProductStatus(ProductStatus.IN_STOCK);
        productRepo.save(product);

        Product updated = productService.updateProductPrice("Chicken Wings", BigDecimal.valueOf(15));
        assertEquals(BigDecimal.valueOf(15), updated.getPrice());
    }

    @Test
    void testDeleteProductById() {
        Product product = new Product();
        product.setName("Chicken Wings");
        product.setPrice(BigDecimal.valueOf(10));
        product.setQuantity(10);
        product.setUnit("kg");
        product.setProductStatus(ProductStatus.IN_STOCK);
        product = productRepo.save(product);

        productService.deleteProductById(product.getProductId());
        assertFalse(productRepo.existsById(product.getProductId()));
    }
}
