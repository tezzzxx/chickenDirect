package org.example.chickendirect.integration.repo;

import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.repos.CustomerRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
public class CustomerRepoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CustomerRepo customerRepo;

    @AfterEach
    void tearDown() {
        customerRepo.deleteAll();
    }

    @Test
    void save_shouldPersistCustomerInPostgres() {
        Customer customer = new Customer();
        customer.setName("Arne Olav");
        customer.setPhoneNumber("12345678");
        customer.setEmail("arne@olav.no");

        Customer saved = customerRepo.save(customer);

        assertThat(saved.getCustomerId()).isNotNull();

        Optional<Customer> found = customerRepo.findById(saved.getCustomerId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("arne@olav.no");
    }

    @Test
    void findByEmail_shouldReturnCustomer_whenEmailExists() {
        Customer customer = new Customer();
        customer.setName("Jennifer Lopez");
        customer.setEmail("jennifer@famous.no");
        customerRepo.save(customer);

        Optional<Customer> result = customerRepo.findByEmail("jennifer@famous.no");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Jennifer Lopez");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        Optional<Customer> result = customerRepo.findByEmail("unknown@test.no");

        assertThat(result).isEmpty();
    }
}
