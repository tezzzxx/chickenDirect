package org.example.chickendirect.services;

import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.repos.AddressRepo;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    private final CustomerService customerService;
    private final AddressRepo addressRepo;

    public AddressService(CustomerService customerService, AddressRepo addressRepo) {
        this.customerService = customerService;
        this.addressRepo = addressRepo;
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
                    newAddress.setCountry(addressDto.country());
                    return newAddress;
                });

        address.setCountry(addressDto.country());
        return addressRepo.save(address);

    }

    public Address findAddressById(long id){
        return addressRepo.findById(id).orElse(null);
    }


    public void deleteAddressById(Long id){
        addressRepo.deleteById(id);
    }
}
