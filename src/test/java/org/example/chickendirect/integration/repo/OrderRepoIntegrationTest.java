package org.example.chickendirect.integration.repo;

import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.entities.Order;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRepoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Test
    void testFindOrderByCustomerId(){
        Customer customer = new Customer("Isabell", "43456753", "isabell@gmail.com",new ArrayList<>());
        customerRepo.save(customer);

        Address address = new Address("12B", "Skogsveien", "0182", "Oslo", "Norway", new ArrayList<>());
        addressRepo.save(address);

        Order order = new Order(
                customer,
                address,
                LocalDate.now(),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10),
                OrderStatus.CONFIRMED,
                new ArrayList<>()
        );
        orderRepo.save(order);

        List<Order> orders = orderRepo.findByCustomerCustomerId(customer.getCustomerId());

        assertThat(orders).isNotEmpty();
        assertThat(orders.getFirst().getCustomer().getCustomerId()).isEqualTo(customer.getCustomerId());
        assertThat(orders.getFirst().getAddress().getAddressId()).isEqualTo(address.getAddressId());
    }

    @Test
    void testExistsByAddress_AddressId(){
        Address address = new Address("2B", "Bekkeveien", "0182", "Oslo", "Norway", new ArrayList<>());
        addressRepo.save(address);

        Customer customer = new Customer("Johanne Hells", "43556745", "jane@example.com", new ArrayList<>());
        customerRepo.save(customer);

        Order order = new Order(customer,
                address,
                LocalDate.now(),
                BigDecimal.valueOf(200.0),
                BigDecimal.valueOf(15.0),
                OrderStatus.CONFIRMED
        );
        orderRepo.save(order);

        boolean exists = orderRepo.existsByAddress_AddressId(address.getAddressId());
        assertThat(exists).isTrue();

        boolean doesntExist = orderRepo.existsByAddress_AddressId(address.getAddressId() + 13443L);
        assertThat(doesntExist).isFalse();
    }

    @Test
    void testExistByCustomer_CustomerId(){
        Address address = new Address("11A", "Solveien", "0182", "Oslo", "Norway", new ArrayList<>());
        addressRepo.save(address);

        Customer customer = new Customer("Pia Nes", "45334567", "pia@example.com", new ArrayList<>());
        customerRepo.save(customer);

        Order order = new Order(customer,
                address,
                LocalDate.now(),
                BigDecimal.valueOf(200.0),
                BigDecimal.valueOf(15.0),
                OrderStatus.CONFIRMED
        );
        orderRepo.save(order);

        boolean exists = orderRepo.existsByCustomer_CustomerId(customer.getCustomerId());
        assertThat(exists).isTrue();

        boolean doesntExist = orderRepo.existsByCustomer_CustomerId(customer.getCustomerId() + 23444L);
        assertThat(doesntExist).isFalse();

    }
}
