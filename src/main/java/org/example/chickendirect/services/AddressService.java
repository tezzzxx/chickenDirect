package org.example.chickendirect.services;

import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepo addressRepo;
    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;

    public AddressService(AddressRepo addressRepo, CustomerRepo customerRepo, OrderRepo orderRepo) {
        this.addressRepo = addressRepo;
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
    }

    @Transactional
    public Address addNewAddress(Long customerId, AddressDto addressDto){
        log.info("Received request to add new address for customerId={}", customerId);

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id={}", customerId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Customer not found with id " + customerId);
                });

        Address newAddress = new Address();
        newAddress.setApartmentNumber(addressDto.apartmentNumber());
        newAddress.setAddress(addressDto.address());
        newAddress.setZipCode(addressDto.zipCode());
        newAddress.setCity(addressDto.city());
        newAddress.setCountry(addressDto.country());

        if(newAddress.getCustomerList() == null){
           newAddress.setCustomerList(new ArrayList<>());
        }
        newAddress.getCustomerList().add(customer);

        if(customer.getAddressList() == null){
            customer.setAddressList(new ArrayList<>());
        }
        customer.getAddressList().add(newAddress);

        Address savedAddress = addressRepo.save(newAddress);
        log.info("Address added successfully for customerId={} with addressId={}", customerId, savedAddress.getAddressId());

        return savedAddress;

    }

    @Transactional
    public Address updateAddress(Long customerId, Long addressId, AddressDto addressDto){
        log.info("Received request to update addressId={} for customerId={}", addressId, customerId);

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id={}", customerId);
                    return new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found");
                });

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address not found with id={}", addressId);
                    return new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found");
                });

        if(!address.getCustomerList().contains(customer)){
            log.warn("Address id={} does not belong to customer id={}", addressId, customerId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address does not belong to this customer");
        }

        if (addressDto.apartmentNumber() != null) address.setApartmentNumber(addressDto.apartmentNumber());
        if (addressDto.address() != null) address.setAddress(addressDto.address());
        if (addressDto.zipCode() != null) address.setZipCode(addressDto.zipCode());
        if (addressDto.city() != null) address.setCity(addressDto.city());
        if (addressDto.country() != null) address.setCountry(addressDto.country());

        Address savedAddress = addressRepo.save(address);
        log.info("Address id={} for customerId={} updated successfully", addressId, customerId);
        return savedAddress;

    }

    public Address findAddressById(long id){
        log.info("Fetching address with id={}", id);

        Address address = addressRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Address not found with id={}", id);
                    return new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found with id " + id);
                });

        log.info("Address id={} retrieved successfully", id);
        return address;
    }

    @Transactional
    public void deleteAddressById(Long id){
        log.info("Received request to delete address with id={}", id);

        Address address = addressRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Address not found with id={}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Address not found with id " + id);
                });

        if (orderRepo.existsByAddress_AddressId(id)){
            log.warn("Address id={} is used in existing orders and cannot be deleted", id);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Address with id " + id + " is used in existing orders and can not be deleted");
        }

        for (Customer customer : address.getCustomerList()) {
            log.info("Removing address id={} from customer id={}", id, customer.getCustomerId());
            customer.getAddressList().remove(address);
        }
        log.info("Address with id={} deleted successfully", id);
        addressRepo.delete(address);
    }
}
