package internal.services;

import internal.entities.Customer;
import internal.repos.CustomerRepo;

import java.util.List;
import java.util.Objects;

public class CustomerService {

    private final CustomerRepo customerRepo;

    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    public List<Customer> findAllCustomers(){
        return customerRepo.findAll();
    }

    public Customer findCustomerById(long id){
        return customerRepo.findById(id).orElse(null);
    }


    public void deleteCustomerById(long id){
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
