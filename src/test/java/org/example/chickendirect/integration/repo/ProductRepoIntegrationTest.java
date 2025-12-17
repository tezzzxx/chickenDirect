package org.example.chickendirect.integration.repo;

import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.ProductRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    void findAllOrderedByIdAsc(){
        Product p1 = new Product("Chicken A", BigDecimal.valueOf(3.99), "kg", ProductStatus.IN_STOCK);
        Product p2 = new Product("Chicken B", BigDecimal.valueOf(4.99), "kg", ProductStatus.IN_STOCK);
    }

}


