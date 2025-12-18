package org.example.chickendirect.unit.service;

import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.ProductRepo;
import org.example.chickendirect.services.ProductService;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ProductServiceUnitTest {

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProduct_success(){
        ProductDto dto = new ProductDto("Chicken Wings", "Tasty wings", BigDecimal.valueOf(10), 20, "kg");
        Product savedProduct = new Product();
        savedProduct.setName(dto.name());
        savedProduct.setDescription(dto.description());
        savedProduct.setPrice(dto.price());
        savedProduct.setQuantity(dto.quantity());
        savedProduct.setUnit(dto.unit());
        savedProduct.setProductStatus(ProductStatus.IN_STOCK);

        when(productRepo.findByName(dto.name())).thenReturn(Optional.empty());
        when(productRepo.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.createProduct(dto);


        assertNotNull(result);
        assertEquals(ProductStatus.IN_STOCK, result.getProductStatus());
        verify(productRepo, times(1)).save(any(Product.class));
    }
}
