package internal.dtos;

import java.util.List;

public record AddressDto (
    String apartmentNumber,
    String address,
    String zipCode,
    String city,
    String country,
    List<CustomerDto> customerList
) {
}
