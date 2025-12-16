package org.example.chickendirect.controllers;

import org.example.chickendirect.dtos.CustomerDto;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.services.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody CustomerDto customerDto) {
        log.info("Received request to create a new customer with email={}", customerDto.email());
        Customer customer = customerService.createCustomer(customerDto);
        log.info("Customer created successfully with id={} and email={}", customer.getCustomerId(), customer.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<Customer> updateCustomerById(
            @PathVariable Long customerId,
            @RequestBody CustomerDto customerDto) {
        log.info("Received request to update customer with id={}", customerId);
        Customer updatedCustomer = customerService.updateCustomerById(customerId, customerDto);
        log.info("Customer with id={} updated successfully", customerId);
        return ResponseEntity.ok(updatedCustomer);
    }

    @GetMapping
    public ResponseEntity<List<Customer>> findAllCustomers() {
        log.info("Received request to fetch all customers");
        List<Customer> customers = customerService.findAllCustomers();
        log.info("Fetched {} customers", customers.size());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> findCustomerById(@PathVariable long id) {
        log.info("Received request to fetch customer with id={}", id);
        Customer customer = customerService.findCustomerById(id);
        log.info("Fetched customer with id={} and email={}", customer.getCustomerId(), customer.getEmail());
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomerById(@PathVariable long id) {
        log.info("Received request to delete customer with id={}", id);
        customerService.deleteCustomerById(id);
        log.info("Customer with id={} deleted successfully", id);
        return ResponseEntity.ok("Customer deleted");
    }

    @PostMapping("/save")
    public ResponseEntity<List<Customer>> saveAllCustomers(@RequestBody List<Customer> customers) {
        log.info("Received request to save a list of customers, total received={}", customers == null ? 0 : customers.size());
        List<Customer> savedCustomers = customerService.saveAllCustomers(customers);
        log.info("Saved {} customers successfully", savedCustomers.size());
        return ResponseEntity.ok(savedCustomers);
    }
}
