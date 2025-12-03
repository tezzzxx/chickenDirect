package internal.services;

import internal.dtos.AddressDto;
import internal.entities.Address;
import internal.repos.AddressRepo;
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
        var newAddress = new Address(addressDto.apartment_number(),addressDto.address(),addressDto.zip_code(),addressDto.city(),addressDto.country(),addressDto.customerList());
        return addressRepo.save(newAddress);
    }

    public Address findAddressById(long id){
        return addressRepo.findById(id).orElse(null);
    }



    public void deleteAddressById(Long id){
        addressRepo.deleteById(id);
    }
}
