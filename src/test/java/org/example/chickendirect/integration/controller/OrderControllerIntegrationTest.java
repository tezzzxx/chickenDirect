package org.example.chickendirect.integration.controller;

import com.jayway.jsonpath.JsonPath;
import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.dtos.OrderInputDto;
import org.example.chickendirect.dtos.OrderOutputDto;
import org.example.chickendirect.dtos.OrderProductInputDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.repos.ProductRepo;
import org.example.chickendirect.services.AddressService;
import org.example.chickendirect.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertFalse;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public class OrderControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private AddressService addressService;

    @Autowired
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Customer savedCustomer;
    private Product savedProduct;
    private Address savedAddress;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setName("Hannah Sval");
        customer.setEmail("HannahS@example.com");
        savedCustomer = customerRepo.save(customer);

        Product product = new Product(
                "Chicken Wings",
                "Wings of chicken",
                BigDecimal.valueOf(59.99),
                ProductStatus.IN_STOCK,
                45,
                "kg");
        savedProduct = productRepo.save(product);

        AddressDto addressDto = new AddressDto(
                null, "12B", "Skogveien", "0182", "Oslo", "Norway", Collections.emptyList()
        );
        savedAddress = addressService.addNewAddress(savedCustomer.getCustomerId(), addressDto);
    }

    @Test
    void testCreateOrder() throws Exception{
        OrderInputDto orderInput = new OrderInputDto(
                savedCustomer.getCustomerId(),
                savedAddress.getAddressId(),
                List.of(new OrderProductInputDto(savedProduct.getProductId(), 5))
        );

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderInput)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customer.customerId").value(savedCustomer.getCustomerId()))
                .andExpect(jsonPath("$.address.addressId").value(savedAddress.getAddressId()))
                .andExpect(jsonPath("$.orderItems[0].productId").value(savedProduct.getProductId()))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(5));
    }

    @Test
    void testFindAllOrders() throws Exception{
        OrderInputDto orderInput = new OrderInputDto(
                savedCustomer.getCustomerId(),
                savedAddress.getAddressId(),
                List.of(new OrderProductInputDto(savedProduct.getProductId(), 5))
        );

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderInput)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/order")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customer.customerId").value(savedCustomer.getCustomerId()))
                .andExpect(jsonPath("$[0].address.addressId").value(savedAddress.getAddressId()))
                .andExpect(jsonPath("$[0].orderItems[0].productId").value(savedProduct.getProductId()))
                .andExpect(jsonPath("$[0].orderItems[0].quantity").value(5));

    }

    @Test
    void testFindOrderById() throws Exception {
        OrderInputDto orderInput = new OrderInputDto(
                savedCustomer.getCustomerId(),
                savedAddress.getAddressId(),
                List.of(new OrderProductInputDto(savedProduct.getProductId(), 5))
        );

        String response = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderInput)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number orderIdNumber = JsonPath.read(response, "$.orderId");
        long orderId = orderIdNumber.longValue();

        mockMvc.perform(get("/api/order/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.customer.customerId").value(savedCustomer.getCustomerId()))
                .andExpect(jsonPath("$.orderItems[0].productId").value(savedProduct.getProductId()))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(5));
    }

    @Test
    void testFindOrdersById_NotFound() throws Exception{

        mockMvc.perform(get("/api/order/1234567"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order not found with id 1234567"));

    }

    @Test
    void testDeleteOrderById() throws Exception{
        OrderInputDto orderInput = new OrderInputDto(
                savedCustomer.getCustomerId(),
                savedAddress.getAddressId(),
                List.of(new OrderProductInputDto(savedProduct.getProductId(), 5))
        );

        OrderOutputDto savedOrder = orderService.createOrder(orderInput);

        mockMvc.perform(delete("/api/order/{orderId}", savedOrder.orderId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Order deleted"));

        assertFalse(orderRepo.findById(savedOrder.orderId()).isPresent());
    }

    @Test
    void testUpdateOrderStatus() throws Exception{
        OrderInputDto orderInput = new OrderInputDto(
                savedCustomer.getCustomerId(),
                savedAddress.getAddressId(),
                List.of(new OrderProductInputDto(savedProduct.getProductId(), 5))
        );

        OrderOutputDto savedOrder = orderService.createOrder(orderInput);

        mockMvc.perform(put("/api/order/{id}/status", savedOrder.orderId())
                        .param("status", OrderStatus.CANCELLED.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.CANCELLED.name()))
                .andExpect(jsonPath("$.orderId").value(savedOrder.orderId()));

    }

    @Test
    void testFindOrderByCustomerId() throws Exception{
        OrderInputDto orderInput1 = new OrderInputDto(
                savedCustomer.getCustomerId(),
                savedAddress.getAddressId(),
                List.of(new OrderProductInputDto(savedProduct.getProductId(), 3))
        );

        OrderInputDto orderInput2 = new OrderInputDto(
                savedCustomer.getCustomerId(),
                savedAddress.getAddressId(),
                List.of(new OrderProductInputDto(savedProduct.getProductId(), 2))
        );

        OrderOutputDto savedOrder1 = orderService.createOrder(orderInput1);
        OrderOutputDto savedOrder2 = orderService.createOrder(orderInput2);

        mockMvc.perform(get("/api/order/customer/{customerId}", savedCustomer.getCustomerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(savedOrder1.orderId()))
                .andExpect(jsonPath("$[1].orderId").value(savedOrder2.orderId()));
    }



}
