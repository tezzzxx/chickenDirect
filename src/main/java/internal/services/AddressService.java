package internal.services;

import internal.dtos.AddressDto;
import internal.dtos.CustomerDto;
import internal.entities.Address;
import internal.entities.Customer;
import internal.repos.AddressRepo;
import internal.repos.CustomerRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
                .findByApartment_numberAndAddressAndZip_codeAndCity(
                        addressDto.apartment_number(),
                        addressDto.address(),
                        addressDto.zip_code(),
                        addressDto.city()
                )
                .orElseGet(() -> {
                    Address newAddress = new Address();
                    newAddress.setApartment_number(addressDto.apartment_number());
                    newAddress.setAddress(addressDto.address());
                    newAddress.setZip_code(addressDto.zip_code());
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
