package org.example.chickendirect.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.dtos.CustomerDto;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.repos.CustomerRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public class CustomerControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepo customerRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void cleanDb() {
        customerRepo.deleteAll();
    }

    @AfterEach
    void tearDown() {
        customerRepo.deleteAll();
    }

    private CustomerDto createCustomerDto(String name, String phone, String email) {
        return new CustomerDto(name, phone, email, List.of());
    }

    @Test
    void createCustomer_shouldReturnCreatedCustomer() throws Exception {
        CustomerDto dto = createCustomerDto("Drake", "12345678", "drake@test.no");

        mockMvc.perform(post("/api/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").isNumber())
                .andExpect(jsonPath("$.name").value("Drake"))
                .andExpect(jsonPath("$.email").value("drake@test.no"));

        List<Customer> customers = customerRepo.findAll();
        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).getEmail()).isEqualTo("drake@test.no");
    }

    @Test
    void updateCustomerById_shouldUpdateExistingCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setName("Justin Timberlake");
        customer.setPhoneNumber("87654321");
        customer.setEmail("justin@whatever.no");
        customer.setAddressList(new ArrayList<>());
        Customer saved = customerRepo.save(customer);

        CustomerDto updateDto = createCustomerDto("Justin Bieber", "87654321", "justin@whatever.no");

        mockMvc.perform(put("/api/customer/{id}", saved.getCustomerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Justin Bieber"));

        Customer updated = customerRepo.findById(saved.getCustomerId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Justin Bieber");
    }

    @Test
    void findAllCustomers_shouldReturnList() throws Exception {
        Customer c1 = new Customer();
        c1.setName("Kendrick");
        c1.setPhoneNumber("12345678");
        c1.setEmail("kendrick@test.no");
        c1.setAddressList(new ArrayList<>());

        Customer c2 = new Customer();
        c2.setName("Sza");
        c2.setPhoneNumber("87654321");
        c2.setEmail("sza@test.no");
        c2.setAddressList(new ArrayList<>());

        customerRepo.save(c1);
        customerRepo.save(c2);

        mockMvc.perform(get("/api/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findCustomerById_shouldReturnCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setName("Russ");
        customer.setPhoneNumber("12345678");
        customer.setEmail("russ@test.no");
        customer.setAddressList(new ArrayList<>());
        Customer saved = customerRepo.save(customer);

        mockMvc.perform(get("/api/customer/{id}", saved.getCustomerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Russ"))
                .andExpect(jsonPath("$.email").value("russ@test.no"));
    }

    @Test
    void deleteCustomerById_shouldDeleteCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setName("Post Malone");
        customer.setPhoneNumber("12345678");
        customer.setEmail("post@malone.no");
        customer.setAddressList(new ArrayList<>());
        Customer saved = customerRepo.save(customer);

        mockMvc.perform(delete("/api/customer/{id}", saved.getCustomerId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer deleted"));

        assertThat(customerRepo.findById(saved.getCustomerId())).isEmpty();
    }

    @Test
    void saveAllCustomers_shouldSaveListOfCustomers() throws Exception {
        Customer c1 = new Customer();
        c1.setName("Megan Stallion");
        c1.setPhoneNumber("12345678");
        c1.setEmail("megan@stallion.no");
        c1.setAddressList(new ArrayList<>());

        Customer c2 = new Customer();
        c2.setName("Tory Lanes");
        c2.setPhoneNumber("87654321");
        c2.setEmail("tory@lanes.no");
        c2.setAddressList(new ArrayList<>());

        List<Customer> customers = List.of(c1, c2);

        mockMvc.perform(post("/api/customer/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customers)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        assertThat(customerRepo.findAll()).hasSize(2);
    }
}
