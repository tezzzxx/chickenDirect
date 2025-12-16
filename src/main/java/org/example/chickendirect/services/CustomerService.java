package org.example.chickendirect.services;

import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.dtos.CustomerDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepo customerRepo;
    private final AddressRepo addressRepo;

    public CustomerService(CustomerRepo customerRepo, AddressRepo addressRepo) {
        this.customerRepo = customerRepo;
        this.addressRepo = addressRepo;}

    @Transactional
    public Customer createCustomer(CustomerDto customerDto) {
        log.info("Creating a new customer with email={}", customerDto.email());

        if (customerRepo.findByEmail(customerDto.email()).isPresent()) {
            log.warn("Customer with email={} already exists", customerDto.email());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Customer with this email already exists, please try again with a different email");
        }

        Customer customer = new Customer();
        customer.setName(customerDto.name());
        customer.setPhoneNumber(customerDto.phoneNumber());
        customer.setEmail(customerDto.email());

        attachAddresses(customer, customerDto.addressList());

        Customer savedCustomer = customerRepo.save(customer);
        log.info("Customer with id={} and email={} created successfully", savedCustomer.getCustomerId(), savedCustomer.getEmail());
        return savedCustomer;
    }

    @Transactional
    public Customer updateCustomerById(Long customerId, CustomerDto customerDto) {
        log.info("Updating customer with id={}", customerId);

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer with id={} not found", customerId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with id " + customerId);
                });

        if (customerDto.email() != null &&
                !customerDto.email().equalsIgnoreCase(customer.getEmail()) &&
                customerRepo.existsByEmailIgnoreCase(customerDto.email())) {

            log.warn("Email {} is already in use", customerDto.email());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        if (customerDto.name() != null) customer.setName(customerDto.name());
        if (customerDto.phoneNumber() != null) customer.setPhoneNumber(customerDto.phoneNumber());
        if (customerDto.email() != null) customer.setEmail(customerDto.email());
        if (customerDto.addressList() != null) attachAddresses(customer, customerDto.addressList());

        Customer updatedCustomer = customerRepo.save(customer);
        log.info("Customer with id={} updated successfully", customerId);
        return updatedCustomer;
    }

    private void attachAddresses(Customer customer, List<AddressDto> addressDtos) {
        if (addressDtos == null || addressDtos.isEmpty()) return;

        if (customer.getAddressList() == null) customer.setAddressList(new ArrayList<>());

        for (AddressDto dto : addressDtos) {
            Address address;

            if (dto.addressId() != null) {
                address = addressRepo.findById(dto.addressId())
                        .orElseThrow(() -> {
                            log.warn("Address with id={} not found", dto.addressId());
                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Address not found with id " + dto.addressId());
                        });

                if (dto.apartmentNumber() != null) address.setApartmentNumber(dto.apartmentNumber());
                if (dto.address() != null) address.setAddress(dto.address());
                if (dto.zipCode() != null) address.setZipCode(dto.zipCode());
                if (dto.city() != null) address.setCity(dto.city());
                if (dto.country() != null) address.setCountry(dto.country());

            } else {
                address = new Address();
                address.setApartmentNumber(dto.apartmentNumber());
                address.setAddress(dto.address());
                address.setZipCode(dto.zipCode());
                address.setCity(dto.city());
                address.setCountry(dto.country());
                address = addressRepo.save(address);
                log.info("New address created with id={}", address.getAddressId());
            }

            linkCustomerAndAddress(customer, address);
        }
    }

    private void linkCustomerAndAddress(Customer customer, Address address) {
        if (address.getCustomerList() == null) address.setCustomerList(new ArrayList<>());
        if (!address.getCustomerList().contains(customer)) address.getCustomerList().add(customer);

        if (customer.getAddressList() == null) customer.setAddressList(new ArrayList<>());
        if (!customer.getAddressList().contains(address)) customer.getAddressList().add(address);
    }

    public List<Customer> findAllCustomers() {
        log.info("Fetching all customers");
        List<Customer> customers = customerRepo.findAll();
        log.info("Fetched {} customers", customers.size());
        return customers;
    }

    public Customer findCustomerById(long id) {
        log.info("Fetching customer with id={}", id);

        return customerRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id={}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Customer not found with id " + id);
                });
    }

    public void deleteCustomerById(long id) {
        log.info("Deleting customer with id={}", id);

        if (!customerRepo.existsById(id)) {
            log.warn("Customer with id={} not found, delete not completed", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Customer not found with id " + id + " delete not completed");
        }

        customerRepo.deleteById(id);
        log.info("Customer with id={} deleted successfully", id);
    }

    public List<Customer> saveAllCustomers(List<Customer> customers) {
        log.info("Saving list of customers, total received={}", customers == null ? 0 : customers.size());

        if (customers == null || customers.isEmpty()) return List.of();

        List<Customer> validCustomers = customers.stream()
                .filter(Objects::nonNull)
                .toList();

        if (validCustomers.isEmpty()) {
            log.warn("No valid customers to save");
            return List.of();
        }

        List<Customer> savedCustomers = customerRepo.saveAll(validCustomers);
        log.info("Saved {} customers successfully", savedCustomers.size());
        return savedCustomers;
    }
}
