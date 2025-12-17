package org.example.chickendirect.integration.repo;

import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.ProductRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class ProductRepoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepo productRepo;

    @Test
    void testSaveAndFindByName(){
        Product product = new Product();
        product.setName("Chicken BA");
        product.setPrice(BigDecimal.valueOf(5.99));
        productRepo.save(product);

        Optional<Product> found = productRepo.findByName("Chicken BA");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Chicken BA");
    }

    @Test
    void testFindByNameNotExists(){
        Optional<Product> found = productRepo.findByName("doesntexist");
        assertThat(found).isNotPresent();
    }

    @Test
    @Transactional
    void testFindByIdForUpdate(){
        Product product = new Product("Chicken Burger", "Test", BigDecimal.valueOf(69.99), ProductStatus.IN_STOCK, 10, "kg");
        productRepo.save(product);

        Optional<Product> lockedProduct = productRepo.findByIdForUpdate(product.getProductId());

        assertThat(lockedProduct).isPresent();
        assertThat(lockedProduct.get().getName()).isEqualTo("Chicken Burger");
    }

}


