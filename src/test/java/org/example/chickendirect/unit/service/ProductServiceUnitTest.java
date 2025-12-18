package org.example.chickendirect.unit.service;

import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.ProductRepo;
import org.example.chickendirect.services.ProductService;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUnitTest {

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private ProductService productService;

    @Nested
    class CreateProductTests {

        @Test
        void testCreateProduct_success() {
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

            verify(productRepo).findByName("Chicken Wings");
            verify(productRepo, times(1)).save(any(Product.class));
        }

        @Test
        void testCreateProduct_alreadyExists() {
            ProductDto dto = new ProductDto("Chicken Wings", "Tasty wings", BigDecimal.valueOf(10), 20, "kg");
            Product existingProduct = new Product();

            when(productRepo.findByName(dto.name())).thenReturn(Optional.of(existingProduct));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
                productService.createProduct(dto);
            });

            assertEquals(409, ex.getStatusCode().value());
            verify(productRepo, never()).save(any());
        }

        @Test
        void testCreateProduct_outOfStock_whenQuantityZero() {
            ProductDto dto = new ProductDto("Chicken Wings", "Tasty wings", BigDecimal.valueOf(10), 0, "kg");

            when(productRepo.findByName("Chicken Wings")).thenReturn(Optional.empty());
            when(productRepo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            Product result = productService.createProduct(dto);

            assertEquals(ProductStatus.OUT_OF_STOCK, result.getProductStatus());
        }

        @Test
        void testCreateProduct_pendingRestock_whenLowStock() {
            ProductDto dto = new ProductDto("Chicken Wings", "Tasty wings", BigDecimal.valueOf(10), 5, "kg");

            when(productRepo.findByName("Chicken Wings")).thenReturn(Optional.empty());
            when(productRepo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            Product result = productService.createProduct(dto);

            assertEquals(ProductStatus.PENDING_RESTOCK, result.getProductStatus());

        }
    }

    @Nested
    class CreateProductsTests {

        @Test
        void testCreateProducts_success() {
            ProductDto dto1 = new ProductDto("Chicken Wings", "Wings", BigDecimal.ONE, 10, "kg");
            ProductDto dto2 = new ProductDto("Chicken Breast", "Breast", BigDecimal.ONE, 10, "kg");

            when(productRepo.findByName(anyString())).thenReturn(Optional.empty());
            when(productRepo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            List<Product> result = productService.createProducts(List.of(dto1, dto2));
            assertEquals(2, result.size());

            verify(productRepo).findByName("Chicken Wings");
            verify(productRepo, times(2)).save(any(Product.class));
        }

        @Test
        void testCreateProducts_whenOneAlreadyExists() {
            ProductDto dto1 = new ProductDto("Chicken Wings", "Wings", BigDecimal.ONE, 10, "kg");
            ProductDto dto2 = new ProductDto("Chicken Breast", "Breast", BigDecimal.ONE, 10, "kg");

            when(productRepo.findByName("Chicken Wings"))
                    .thenReturn(Optional.empty());
            when(productRepo.findByName("Chicken Breast"))
                    .thenReturn(Optional.of(new Product()));

            when(productRepo.save(any(Product.class)))
                    .thenAnswer(i -> i.getArgument(0));

            assertThrows(ResponseStatusException.class,
                    () -> productService.createProducts(List.of(dto1, dto2)));

            verify(productRepo, times(1)).save(any(Product.class));

        }
    }

    @Nested
    class FindByProductIdTests {

        @Test
        void testFindProductById_found() {
            Product product = new Product();
            product.setProductId(2L);
            when(productRepo.findById(2L)).thenReturn(Optional.of(product));

            Product result = productService.findProductById(2L);

            assertNotNull(result);
            assertEquals(2L, result.getProductId());
        }

        @Test
        void testFindProductById_notFound() {
            when(productRepo.findById(122L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> productService.findProductById(122L)
            );

            assertEquals(404, ex.getStatusCode().value());
        }

    }

    @Nested
    class FindAllProductsTests {

        @Test
        void testFindAllProducts() {
            Product p1 = new Product();
            Product p2 = new Product();
            when(productRepo.findAll()).thenReturn(List.of(p1, p2));

            List<Product> products = productService.findAllProducts();

            assertEquals(2, products.size());
        }

        @Test
        void testFindAllProducts_whenEmpty() {

            when(productRepo.findAll()).thenReturn(List.of());

            List<Product> result = productService.findAllProducts();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class UpdateProductPriceTests {

        @Test
        void testUpdateProductPrice_success() {
            Product product = new Product();
            product.setName("Chicken Wings");
            product.setPrice(BigDecimal.valueOf(10));

            when(productRepo.findByName("Chicken Wings")).thenReturn(Optional.of(product));
            when(productRepo.save(product)).thenReturn(product);

            Product updated = productService.updateProductPrice("Chicken Wings", BigDecimal.valueOf(15));

            assertEquals(BigDecimal.valueOf(15), updated.getPrice());
            verify(productRepo, times(1)).save(product);
        }

        @Test
        void testUpdateProductPrice_invalidPrice() {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    productService.updateProductPrice("Chicken Wings", BigDecimal.valueOf(-1002))
            );

            assertEquals(400, ex.getStatusCode().value());
            verify(productRepo, never()).save(any());
        }

        @Test
        void testUpdateProductPrice_nullPrice() {
            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> productService.updateProductPrice("Chicken", null)
            );

            assertEquals(400, ex.getStatusCode().value());

        }

        @Test
        void testUpdatePrice_productNotFound() {
            when(productRepo.findByName("Chicken")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> productService.updateProductPrice("Chicken", BigDecimal.TEN)
            );
            assertEquals(404, ex.getStatusCode().value());

        }
    }

    @Nested
    class UpdateProductStatusTests {

        @Test
        void testUpdateProductStatus_success() {
            Product product = new Product();
            product.setProductStatus(ProductStatus.IN_STOCK);

            when(productRepo.findByName("Chicken")).thenReturn(Optional.of(product));
            when(productRepo.save(product)).thenReturn(product);

            Product updated = productService.updateProductStatus("Chicken", ProductStatus.OUT_OF_STOCK);

            assertEquals(ProductStatus.OUT_OF_STOCK, updated.getProductStatus());
        }

        @Test
        void testUpdateProductStatus_nullStatus() {
            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> productService.updateProductStatus("Chicken", null)

            );

            assertEquals(400, ex.getStatusCode().value());
            verify(productRepo, never()).save(any());
        }

        @Test
        void testUpdateProductStatus_notFound() {
            when(productRepo.findByName("Chicken")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> productService.updateProductStatus("Chicken", ProductStatus.IN_STOCK)
            );
            assertEquals(404, ex.getStatusCode().value());

        }
    }

    @Nested
    class UpdateProductQuantityTests {

        @Test
        void testUpdateProductQuantity_nullQuantity() {
            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> productService.updateProductQuantity(1L, null)
            );

            assertEquals(400, ex.getStatusCode().value());
        }

        @Test
        void testUpdateProductQuantity_notFound() {
            when(productRepo.findById(100L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> productService.updateProductQuantity(100L, 10)
            );

            assertEquals(404, ex.getStatusCode().value());

        }

        @Test
        void testUpdateProductQuantity_outOfStock() {
            Product product = new Product();

            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(productRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            Product updated = productService.updateProductQuantity(1L, 0);

            assertEquals(ProductStatus.OUT_OF_STOCK, updated.getProductStatus());
        }

        @Test
        void testUpdateProductQuantity_pendingRestock() {
            Product product = new Product();

            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(productRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            Product updated = productService.updateProductQuantity(1L, 2);

            assertEquals(ProductStatus.PENDING_RESTOCK, updated.getProductStatus());
        }
    }

    @Nested
    class DeleteProductByIdTests {

        @Test
        void testDeleteProductById_success() {
            when(productRepo.existsById(100L)).thenReturn(true);

            productService.deleteProductById(100L);
            verify(productRepo).deleteById(100L);

        }

        @Test
        void testDeleteProductById_notFound() {
            when(productRepo.existsById(1L)).thenReturn(false);

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> productService.deleteProductById(1L)
            );

            assertEquals(404, ex.getStatusCode().value());
        }
    }

}
