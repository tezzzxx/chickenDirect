package org.example.chickendirect.services;

import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.dtos.CustomerDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CustomerService {

    private final CustomerRepo customerRepo;
    private final AddressRepo addressRepo;

    public CustomerService(CustomerRepo customerRepo, AddressRepo addressRepo) {
        this.customerRepo = customerRepo;
        this.addressRepo = addressRepo;
    }

    public Customer createCustomer(CustomerDto customerDto){

        Customer newCustomer = new Customer();
        newCustomer.setName(customerDto.name());
        newCustomer.setPhoneNumber(customerDto.phoneNumber());
        newCustomer.setEmail(customerDto.email());

        List<Address> addresses = new ArrayList<>();

        for(AddressDto addressDto : customerDto.addressList()){
            Address address = addressRepo
                    .findByApartmentNumberAndAddressAndZipCodeAndCity(
                            addressDto.apartmentNumber(),
                            addressDto.address(),
                            addressDto.zipCode(),
                            addressDto.city()
                    )
                    .orElseGet(() -> {
                        Address newAddress= new Address();
                        newAddress.setApartmentNumber(addressDto.apartmentNumber());
                        newAddress.setAddress(addressDto.address());
                        newAddress.setZipCode(addressDto.zipCode());
                        newAddress.setCity(addressDto.city());
                        newAddress.setCountry(addressDto.country());
                        return newAddress;
                    });
            if(address.getCustomerList() == null){
                address.setCustomerList(new ArrayList<>());
            }
            address.getCustomerList().add(newCustomer);
            addresses.add(address);
        }

        newCustomer.setAddressList(addresses);
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
