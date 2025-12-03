package internal.dtos;

import internal.entities.Customer;

import java.util.List;

public record AddressDto (
    String apartment_number,
    String address,
    String zip_code,
    String city,
    String country,
    List<Customer> customerList
) {

}
