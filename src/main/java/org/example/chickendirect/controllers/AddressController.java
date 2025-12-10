package org.example.chickendirect.controllers;

import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.services.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;


    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody AddressDto addressDto){
        return ResponseEntity.ok(addressService.createAddress(addressDto));
    }

    @GetMapping("/address/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable long id){
        return ResponseEntity.ok(addressService.findAddressById(id));
    }

    @DeleteMapping("address/{id}")
    public ResponseEntity<String> deleteAddressById(@PathVariable long id){
        addressService.deleteAddressById(id);
        return ResponseEntity.ok("Address deleted");
    }
}
