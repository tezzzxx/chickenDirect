package org.example.chickendirect.integration.service;


import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.dtos.OrderInputDto;
import org.example.chickendirect.dtos.OrderOutputDto;
import org.example.chickendirect.dtos.OrderProductInputDto;
import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.repos.ProductRepo;
import org.example.chickendirect.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public class OrderServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderService orderService;

    @Test
    void testCreateOrderFailsWhenStockIsInsufficient() throws Exception{

        Customer customer = new Customer();
        customer.setName("Hannah Sval");
        customer.setPhoneNumber("98334567");
        customer.setEmail("HannahS@test.com");

        Address address = new Address();
        address.setApartmentNumber("2B");
        address.setAddress("Wasvei");
        address.setZipCode("0182");
        address.setCity("Oslo");
        address.setCountry("Norway");

        Product product = new Product();
        product.setName("Chicken Wings");
        product.setDescription("Fresh wings");
        product.setPrice(BigDecimal.valueOf(50));
        product.setQuantity(5);
        product.setUnit("kg");
        product.setProductStatus(ProductStatus.IN_STOCK);
        product = productRepo.save(product);

        customer.setAddressList(List.of(address));
        customer = customerRepo.save(customer);


        OrderProductInputDto orderItem = new OrderProductInputDto(product.getProductId(), 10);
        OrderInputDto input = new OrderInputDto(
                customer.getCustomerId(),
                address.getAddressId(),
                List.of(orderItem)
        );

        assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(input)
        );

        assertTrue(orderRepo.findAll().isEmpty());

        Product unchangedProduct =
                productRepo.findById(product.getProductId()).orElseThrow();
        assertEquals(5, unchangedProduct.getQuantity());

    }

    @Test
    void testCreateOrderSuccessfullyReducesStockAndCalculatesShipping() throws Exception{
        Customer customer = new Customer();
        customer.setName("Hannah Sval");
        customer.setPhoneNumber("98334567");
        customer.setEmail("HannahS@test.com");

        Address address = new Address();
        address.setApartmentNumber("2B");
        address.setAddress("Wasvei");
        address.setZipCode("0182");
        address.setCity("Oslo");
        address.setCountry("Norway");

        Product product = new Product();
        product.setName("Chicken Wings");
        product.setDescription("Fresh wings");
        product.setPrice(BigDecimal.valueOf(50));
        product.setQuantity(15);
        product.setUnit("kg");
        product.setProductStatus(ProductStatus.IN_STOCK);
        product = productRepo.save(product);

        customer.setAddressList(List.of(address));
        customer = customerRepo.save(customer);


        OrderProductInputDto orderItem = new OrderProductInputDto(product.getProductId(), 5);
        OrderInputDto input = new OrderInputDto(
                customer.getCustomerId(),
                address.getAddressId(),
                List.of(orderItem)
        );

        OrderOutputDto createdOrder = orderService.createOrder(input);

        assertTrue(createdOrder.orderId() > 0);
        assertEquals(1, orderRepo.findAll().size());

        Product updatedProduct = productRepo.findById(product.getProductId()).orElseThrow();
        assertEquals(10, updatedProduct.getQuantity());

        assertEquals(ProductStatus.PENDING_RESTOCK, updatedProduct.getProductStatus());

        BigDecimal expectedTotal = product.getPrice().multiply(BigDecimal.valueOf(5));
        assertEquals(expectedTotal, createdOrder.totalSum());

    }


}
