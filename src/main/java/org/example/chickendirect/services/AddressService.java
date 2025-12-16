package org.example.chickendirect.services;

import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Service
public class AddressService {

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
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found with id " + customerId)
                );

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

        return  addressRepo.save(newAddress);

    }

    @Transactional
    public Address updateAddress(Long customerId, Long addressId, AddressDto addressDto){
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"
                ));

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found"
                ));
        if(!address.getCustomerList().contains(customer)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address does not belong to this customer");
        }

        if (addressDto.apartmentNumber() != null) address.setApartmentNumber(addressDto.apartmentNumber());
        if (addressDto.address() != null) address.setAddress(addressDto.address());
        if (addressDto.zipCode() != null) address.setZipCode(addressDto.zipCode());
        if (addressDto.city() != null) address.setCity(addressDto.city());
        if (addressDto.country() != null) address.setCountry(addressDto.country());

        return addressRepo.save(address);

    }

    public Address findAddressById(long id){
        return addressRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found with id " + id));
    }

    @Transactional
    public void deleteAddressById(Long id){

        Address address = addressRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found with id " + id));

        if (orderRepo.existsByAddress_AddressId(id)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Address with id " + id + " is used in existing orders and can not be deleted");
        }

        for (Customer customer : address.getCustomerList()) {
            customer.getAddressList().remove(address);
        }
        addressRepo.delete(address);
    }
}
