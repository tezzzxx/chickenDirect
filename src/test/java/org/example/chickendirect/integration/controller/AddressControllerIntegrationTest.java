package org.example.chickendirect.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.dtos.AddressDto;

import org.example.chickendirect.entities.Address;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.services.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public class AddressControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AddressService addressService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john.doe@example.com");
        savedCustomer= customerRepo.save(customer);
    }

    @Test
    void testAddNewAddress() throws Exception{
        AddressDto addressDto = new AddressDto(
                null,
                "11B",
                "Vannveien",
                "0182",
                "Oslo",
                "Norway",
                null
        );

        mockMvc.perform(post("/api/address/{customerId}", savedCustomer.getCustomerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apartmentNumber").value("11B"))
                .andExpect(jsonPath("$.address").value("Vannveien"))
                .andExpect(jsonPath("$.zipCode").value("0182"))
                .andExpect(jsonPath("$.city").value("Oslo"))
                .andExpect(jsonPath("$.country").value("Norway"))
                .andExpect(jsonPath("$.addressId").isNumber());

    }

    @Test
    void testGetAddressById() throws Exception {
        Address savedAddress = addressService.addNewAddress(savedCustomer.getCustomerId(),
                new AddressDto(
                        null, "12B", "Skogveien", "0182", "Oslo", "Norway", Collections.emptyList()
                ));

        mockMvc.perform(get("/api/address/{id}", savedAddress.getAddressId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apartmentNumber").value("12B"))
                .andExpect(jsonPath("$.address").value("Skogveien"))
                .andExpect(jsonPath("$.zipCode").value("0182"))
                .andExpect(jsonPath("$.city").value("Oslo"))
                .andExpect(jsonPath("$.country").value("Norway"));
    }

    @Test
    void testUpdateAddress() throws Exception {
        Address savedAddress = addressService.addNewAddress(savedCustomer.getCustomerId(),
                new AddressDto(
                        null, "12B", "Skogveien", "0182", "Oslo", "Norway", Collections.emptyList()
                ));

        AddressDto updatedDto = new AddressDto(
                null, "14B", "Valkersvei", "0182", "Oslo", "Norway", Collections.emptyList()
        );

        mockMvc.perform(put("/api/address/{customerId}/{addressId}",savedCustomer.getCustomerId(), savedAddress.getAddressId())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apartmentNumber").value("14B"))
                .andExpect(jsonPath("$.address").value("Valkersvei"))
                .andExpect(jsonPath("$.zipCode").value("0182"))
                .andExpect(jsonPath("$.city").value("Oslo"))
                .andExpect(jsonPath("$.country").value("Norway"));
    }

    @Test
    void testDeleteAddressById() throws Exception{
        Address savedAddress = addressService.addNewAddress(savedCustomer.getCustomerId(),
                new AddressDto(null, "14B", "Valkersvei", "0182", "Oslo", "Norway", Collections.emptyList())
        );

        mockMvc.perform(delete("/api/address/{id}", savedAddress.getAddressId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Address deleted"));

        mockMvc.perform(get("/api/address/{id}", savedAddress.getAddressId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());


    }

}
