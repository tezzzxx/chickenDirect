package org.example.chickendirect.services;

import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    private final CustomerService customerService;
    private final AddressRepo addressRepo;
    private final CustomerRepo customerRepo;

    public AddressService(CustomerService customerService, AddressRepo addressRepo, CustomerRepo customerRepo) {
        this.customerService = customerService;
        this.addressRepo = addressRepo;
        this.customerRepo = customerRepo;
    }

    public Address createAddress(AddressDto addressDto){

        Address address = addressRepo
                .findByApartmentNumberAndAddressAndZipCodeAndCity(
                        addressDto.apartmentNumber(),
                        addressDto.address(),
                        addressDto.zipCode(),
                        addressDto.city()
                )
                .orElseGet(() -> {
                    Address newAddress = new Address();
                    newAddress.setApartmentNumber(addressDto.apartmentNumber());
                    newAddress.setAddress(addressDto.address());
                    newAddress.setZipCode(addressDto.zipCode());
                    newAddress.setCity(addressDto.city());
                    return newAddress;
                });

        return addressRepo.save(address);

    }

    public Address findAddressById(long id){
        return addressRepo.findById(id).orElse(null);
    }


    public void deleteAddressById(Long id){
        addressRepo.deleteById(id);
    }
}
