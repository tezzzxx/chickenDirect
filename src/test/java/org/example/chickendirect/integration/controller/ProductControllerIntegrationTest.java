package org.example.chickendirect.integration.controller;

import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.dtos.UpdateProductQuantity;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.ProductRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepo productRepo;

    @BeforeEach
    void cleanUp(){
        productRepo.deleteAll();
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCreateProduct() throws Exception{
        ProductDto productDto = new ProductDto("Chicken Burger", "Delicious chicken burger", BigDecimal.valueOf(5.99),10, "kg");

        mockMvc.perform(post("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Chicken Burger"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void testCreateProductsBatch() throws Exception{
        List<ProductDto> productDtos = List.of(
                new ProductDto("Chicken Feet", "Feet of chicken", BigDecimal.valueOf(79.99), 12, "kg"),
                new ProductDto("Chicken Wings", "Wings of chicken", BigDecimal.valueOf(59.99), 45, "kg")
        );
        mockMvc.perform(post("/api/product/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(productDtos)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[*].name", hasItem("Chicken Feet")))
                .andExpect(jsonPath("$[*].name", hasItem("Chicken Wings")));

        assertEquals(2, productRepo.findAll().size());

    }

    private static String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetAllProducts() throws Exception {
        Product product = new Product("Chicken Feet", "Feet of chicken", BigDecimal.valueOf(79.99), ProductStatus.IN_STOCK, 12, "kg");
        productRepo.save(product);

        mockMvc.perform(get("/api/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Chicken Feet")));
    }

    @Test
    void testUpdateProductStatus() throws Exception{
        Product product = new Product("Chicken Feet", "Feet of chicken", BigDecimal.valueOf(79.99), ProductStatus.IN_STOCK, 12, "kg");
        productRepo.save(product);

        String patchJson = """
            {
                "newStatus": "PENDING_RESTOCK"
            }
        """;

        mockMvc.perform(patch("/api/product/Chicken Feet/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Feet"))
                .andExpect(jsonPath("$.productStatus").value("PENDING_RESTOCK"));
    }

    @Test
    void testUpdateProductPrice() throws Exception{
        Product product = new Product("Chicken Feet", "Feet of chicken", BigDecimal.valueOf(79.99), ProductStatus.IN_STOCK, 12, "kg");
        productRepo.save(product);

        String patchJson = """
            {
                "newPrice": 89.99
            }
        """;

        mockMvc.perform(patch("/api/product/Chicken Feet/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Feet"))
                .andExpect(jsonPath("$.price").value(89.99));
    }

    @Test
    void testUpdateProductQuantity() throws Exception{
        Product product = new Product("Chicken Feet", "Feet of chicken", BigDecimal.valueOf(79.99), ProductStatus.IN_STOCK, 12, "kg");
        productRepo.save(product);

       UpdateProductQuantity request = new UpdateProductQuantity(1000);

        mockMvc.perform(patch("/api/product/{id}/quantity", product.getProductId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(1000))
                .andExpect(jsonPath("$.name").value("Chicken Feet"));
    }

    @Test
    void testFindAllProducts() throws Exception{

        Product product1 = new Product("Chicken Feet", "Feet of chicken", BigDecimal.valueOf(79.99), ProductStatus.IN_STOCK, 12, "kg");
        Product product2 = new Product("Chicken Wings", "Wings of chicken", BigDecimal.valueOf(59.99), ProductStatus.IN_STOCK, 45, "kg");
        productRepo.save(product1);
        productRepo.save(product2);

        mockMvc.perform(get("/api/product")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Chicken Feet", "Chicken Wings")))
                .andExpect(jsonPath("$[*].quantity", hasItems(12, 45)));
    }


    @Test
    void testFindProductById() throws Exception{
        Product product = new Product("Chicken Wings", "Wings of chicken", BigDecimal.valueOf(59.99), ProductStatus.IN_STOCK, 45, "kg");
        productRepo.save(product);

        mockMvc.perform(get("/api/product/{id}", product.getProductId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Wings"))
                .andExpect(jsonPath("$.quantity").value(45))
                .andExpect(jsonPath("$.price").value(59.99));
    }

    @Test
    void testDeleteProductById() throws Exception{
        Product product = new Product("Chicken Wings", "Wings of chicken", BigDecimal.valueOf(59.99), ProductStatus.IN_STOCK, 45, "kg");
        productRepo.save(product);

        mockMvc.perform(delete("/api/product/{id}", product.getProductId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted"));

        assertFalse(productRepo.findById(product.getProductId()).isPresent());

    }

}
