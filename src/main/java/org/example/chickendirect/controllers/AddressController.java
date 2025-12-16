package org.example.chickendirect.controllers;

import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.services.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/address")
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/{customerId}")
    public ResponseEntity<Address> addNewAddress(
            @PathVariable Long customerId,
            @RequestBody AddressDto addressDto){

        log.info("Received request to add new address for customerId={}", customerId);
        Address savedAddress = addressService.addNewAddress(customerId, addressDto);
        log.info("Address added successfully for customerId={} with addressId={}", customerId, savedAddress.getAddressId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
    }

    @PutMapping("/{customerId}/{addressId}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @RequestBody AddressDto addressDto){

        log.info("Received request to update addressId={} for customerId={}", addressId, customerId);
        Address updatedAddress = addressService.updateAddress(customerId, addressId, addressDto);
        log.info("Address id={} updated successfully for customerId={}", addressId, customerId);
        return ResponseEntity.ok(updatedAddress);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable long id){
        log.info("Fetching address with id={}", id);
        Address address = addressService.findAddressById(id);
        log.info("Address id={} retrieved successfully", id);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddressById(@PathVariable long id){
        log.info("Received request to delete address with id={}", id);
        addressService.deleteAddressById(id);
        log.info("Address id={} deleted successfully", id);
        return ResponseEntity.ok("Address deleted");
    }
}
