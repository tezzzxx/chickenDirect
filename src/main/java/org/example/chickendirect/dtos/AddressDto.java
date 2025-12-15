package org.example.chickendirect.dtos;

import java.util.List;

public record AddressDto (
        Long addressId,
        String apartmentNumber,
        String address,
        String zipCode,
        String city,
        String country,
        List<CustomerDto> customerList
) {
}
