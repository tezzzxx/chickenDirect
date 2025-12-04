package internal.services;

import internal.dtos.CustomerDto;
import internal.entities.Customer;
import internal.repos.CustomerRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class CustomerService {

    private final CustomerRepo customerRepo;

    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    public Customer createCustomer(CustomerDto customerDto){
        var newCustomer = new Customer(
                customerDto.name(),
                customerDto.phoneNumber(),
                customerDto.email(),
                customerDto.addressList()
        );
        return customerRepo.save(newCustomer);
    }

    public List<Customer> findAllCustomers(){
        return customerRepo.findAll();
    }

    public Customer findCustomerById(long id){
        return customerRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found with id " + id));
    }

    public void deleteCustomerById(long id){
        if (!customerRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with id " + id);
        }
        customerRepo.deleteById(id);
    }

    public List<Customer> saveAllCustomers(List<Customer> customers){
        if (customers == null || customers.isEmpty()) {
            return  List.of();
        }

        List<Customer> validCustomers = customers.stream()
                .filter(Objects::nonNull)
                .toList();

        if (validCustomers.isEmpty()){
            return List.of();
        }
        return customerRepo.saveAll(validCustomers);
    }
}
