package org.example.chickendirect.dtos;

public record AddressOutputDto(
        long addressId,
        String apartmentNumber,
        String address,
        String zipCode,
        String city,
        String country
) {
}
