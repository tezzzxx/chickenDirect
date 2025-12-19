package org.example.chickendirect.integration.controller;

import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.entities.*;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.repos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public class OrderProductControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderProductRepo orderProductRepo;

    private Customer customer;
    private Order order;
    private Product product;
    private OrderProduct orderProduct;

    @BeforeEach
    void setup() {
        orderProductRepo.deleteAll();
        orderRepo.deleteAll();
        customerRepo.deleteAll();
        addressRepo.deleteAll();
        productRepo.deleteAll();

        customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail("test@example.com");
        customer.setPhoneNumber("12345678");
        customerRepo.save(customer);

        Address address = new Address();
        address.setApartmentNumber("1A");
        address.setAddress("Test Street");
        address.setZipCode("1234");
        address.setCity("Oslo");
        address.setCountry("Norway");
        addressRepo.save(address);

        order = new Order();
        order.setCustomer(customer);
        order.setAddress(address);
        order.setDate(LocalDate.now());
        order.setTotalSum(BigDecimal.valueOf(500));
        order.setShippingCharge(BigDecimal.valueOf(50));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order);

        product = new Product();
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setQuantity(100); // gi nok stock
        productRepo.save(product);

        orderProduct = new OrderProduct();
        orderProduct.setOrder(order);
        orderProduct.setProduct(product);
        orderProduct.setQuantity(2);
        orderProduct.setUnitPrice(BigDecimal.valueOf(100));
        orderProductRepo.save(orderProduct);

        productRepo.flush();
        orderProductRepo.flush();
    }

    @Test
    void getOrderProducts_shouldReturnProducts() throws Exception {
        mockMvc.perform(get("/api/orderProduct/{orderId}/products", order.getOrderId())
                        .param("email", customer.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].unitPrice").value(100))
                .andExpect(jsonPath("$[0].totalPrice").value(200));
    }

    @Test
    void updateOrderProductQuantity_shouldUpdateQuantity() throws Exception {
        mockMvc.perform(put("/api/orderProduct/{orderId}/products/updateOrder", order.getOrderId())
                        .param("productName", product.getName())
                        .param("newQuantity", "3")
                        .param("email", customer.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.unitPrice").value(100))
                .andExpect(jsonPath("$.totalPrice").value(300));
    }

    @Test
    void addProductToOrder_shouldAddProduct() throws Exception {
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setPrice(BigDecimal.valueOf(50));
        newProduct.setQuantity(100); // gi nok stock
        productRepo.save(newProduct);
        productRepo.flush();

        mockMvc.perform(post("/api/orderProduct/{orderId}/productsAdd", order.getOrderId())
                        .param("productId", String.valueOf(newProduct.getProductId()))
                        .param("quantity", "4")
                        .param("email", customer.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.quantity").value(4))
                .andExpect(jsonPath("$.unitPrice").value(50))
                .andExpect(jsonPath("$.totalPrice").value(200));
    }

    @Test
    void getAllOrderProducts_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/orderProduct/findAll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].unitPrice").value(100))
                .andExpect(jsonPath("$[0].totalPrice").value(200));
    }

    @Test
    void getOrderProductById_shouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/orderProduct/{id}", orderProduct.getOrderProductId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.unitPrice").value(100))
                .andExpect(jsonPath("$.totalPrice").value(200));
    }
}