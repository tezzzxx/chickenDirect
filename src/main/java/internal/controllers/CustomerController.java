package internal.controllers;

import internal.dtos.CustomerDto;
import internal.entities.Customer;
import internal.services.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> findAllCustomers(){
        return ResponseEntity.ok(customerService.findAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> findCustomerById(@PathVariable long id){
        return ResponseEntity.ok(customerService.findCustomerById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomerById(@PathVariable long id){
        customerService.deleteCustomerById(id);
        return ResponseEntity.ok("Customer deleted");
    }

    @PostMapping
    public ResponseEntity<List<Customer>> saveAllCustomers(@RequestBody Customer customer){
        return ResponseEntity.ok(customerService.saveAllCustomers((List<Customer>) customer));
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody CustomerDto customerDto){
        return ResponseEntity.ok(customerService.createCustomer(customerDto));
    }

}
