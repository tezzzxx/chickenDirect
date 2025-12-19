package org.example.chickendirect.unit.service;

import org.example.chickendirect.dtos.CustomerDto;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.services.CustomerService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceUnitTest {
    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private OrderRepo orderRepo;

    @InjectMocks
    private CustomerService customerService;

    @Nested
    class CreateCustomerTests {

        @Test
        void testCreateCustomer_success() {
            CustomerDto dto = new CustomerDto("John Doe", "12345678", "john@example.com", List.of());
            Customer savedCustomer = new Customer();
            savedCustomer.setCustomerId(1L);
            savedCustomer.setName(dto.name());
            savedCustomer.setEmail(dto.email());
            savedCustomer.setPhoneNumber(dto.phoneNumber());

            when(customerRepo.findByEmail(dto.email())).thenReturn(Optional.empty());
            when(customerRepo.save(any(Customer.class))).thenReturn(savedCustomer);

            Customer result = customerService.createCustomer(dto);

            assertNotNull(result);
            assertEquals(1L, result.getCustomerId());
            verify(customerRepo).save(any(Customer.class));
        }

        @Test
        void testCreateCustomer_emailAlreadyExists() {
            CustomerDto dto = new CustomerDto("John Doe", "12345678", "john@example.com", List.of());
            when(customerRepo.findByEmail(dto.email())).thenReturn(Optional.of(new Customer()));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> customerService.createCustomer(dto));

            assertEquals(409, ex.getStatusCode().value());
            verify(customerRepo, never()).save(any());
        }
    }

    @Nested
    class UpdateCustomerTests {

        @Test
        void testUpdateCustomer_success() {
            Customer existing = new Customer();
            existing.setCustomerId(1L);
            existing.setEmail("old@example.com");
            existing.setName("Old Name");
            existing.setPhoneNumber("11111111");

            CustomerDto dto = new CustomerDto("New Name", "22222222", "new@example.com", null);

            when(customerRepo.findById(1L)).thenReturn(Optional.of(existing));
            when(customerRepo.existsByEmailIgnoreCase(dto.email())).thenReturn(false);
            when(customerRepo.save(existing)).thenReturn(existing);

            Customer updated = customerService.updateCustomerById(1L, dto);

            assertEquals("New Name", updated.getName());
            assertEquals("22222222", updated.getPhoneNumber());
            assertEquals("new@example.com", updated.getEmail());
        }

        @Test
        void testUpdateCustomer_notFound() {
            when(customerRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> customerService.updateCustomerById(1L, new CustomerDto("Name", "123", "a@b.com", null)));

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        void testUpdateCustomer_emailConflict() {
            Customer existing = new Customer();
            existing.setCustomerId(1L);
            existing.setEmail("old@example.com");

            CustomerDto dto = new CustomerDto("Name", "123", "conflict@example.com", null);

            when(customerRepo.findById(1L)).thenReturn(Optional.of(existing));
            when(customerRepo.existsByEmailIgnoreCase(dto.email())).thenReturn(true);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> customerService.updateCustomerById(1L, dto));

            assertEquals(409, ex.getStatusCode().value());
        }
    }

    @Nested
    class FindCustomerTests {

        @Test
        void testFindAllCustomers() {
            when(customerRepo.findAll()).thenReturn(List.of(new Customer(), new Customer()));

            List<Customer> customers = customerService.findAllCustomers();

            assertEquals(2, customers.size());
        }

        @Test
        void testFindCustomerById_found() {
            Customer customer = new Customer();
            customer.setCustomerId(1L);
            when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));

            Customer result = customerService.findCustomerById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getCustomerId());
        }

        @Test
        void testFindCustomerById_notFound() {
            when(customerRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> customerService.findCustomerById(1L));

            assertEquals(404, ex.getStatusCode().value());
        }
    }

    @Nested
    class DeleteCustomerTests {

        @Test
        void testDeleteCustomer_success() {
            when(customerRepo.existsById(1L)).thenReturn(true);
            when(orderRepo.existsByCustomer_CustomerId(1L)).thenReturn(false);

            customerService.deleteCustomerById(1L);

            verify(customerRepo).deleteById(1L);
        }

        @Test
        void testDeleteCustomer_notFound() {
            when(customerRepo.existsById(1L)).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> customerService.deleteCustomerById(1L));

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        void testDeleteCustomer_withOrders() {
            when(customerRepo.existsById(1L)).thenReturn(true);
            when(orderRepo.existsByCustomer_CustomerId(1L)).thenReturn(true);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> customerService.deleteCustomerById(1L));

            assertEquals(400, ex.getStatusCode().value());
        }
    }

    @Nested
    class SaveAllCustomersTests {

        @Test
        void testSaveAllCustomers_success() {
            Customer c1 = new Customer();
            c1.setName("A");
            c1.setEmail("a@example.com");
            c1.setPhoneNumber("123");
            Customer c2 = new Customer();
            c2.setName("B");
            c2.setEmail("b@example.com");
            c2.setPhoneNumber("456");

            when(customerRepo.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(customerRepo.existsByPhoneNumber(anyString())).thenReturn(false);
            when(customerRepo.saveAll(anyList())).thenReturn(List.of(c1, c2));

            List<Customer> result = customerService.saveAllCustomers(List.of(c1, c2));

            assertEquals(2, result.size());
        }

        @Test
        void testSaveAllCustomers_missingRequiredFields() {
            Customer invalid = new Customer();
            invalid.setName(null);
            invalid.setEmail("email@example.com");
            invalid.setPhoneNumber("123");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> customerService.saveAllCustomers(List.of(invalid)));

            assertEquals(400, ex.getStatusCode().value());
        }

        @Test
        void testSaveAllCustomers_conflictingEmail() {
            Customer c = new Customer();
            c.setName("Test");
            c.setEmail("exists@example.com");
            c.setPhoneNumber("123");

            when(customerRepo.existsByEmailIgnoreCase("exists@example.com")).thenReturn(true);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> customerService.saveAllCustomers(List.of(c)));

            assertEquals(409, ex.getStatusCode().value());
        }

        @Test
        void testSaveAllCustomers_conflictingPhone() {
            Customer c = new Customer();
            c.setName("Test");
            c.setEmail("new@example.com");
            c.setPhoneNumber("exists-phone");

            when(customerRepo.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
            when(customerRepo.existsByPhoneNumber("exists-phone")).thenReturn(true);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> customerService.saveAllCustomers(List.of(c)));

            assertEquals(409, ex.getStatusCode().value());
        }
    }
}
