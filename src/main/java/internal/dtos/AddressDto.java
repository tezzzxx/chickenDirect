package internal.dtos;

import java.util.List;

public record AddressDto (
    String apartment_number,
    String address,
    String zip_code,
    String city,
    String country,
    List<CustomerDto> customerList
) {
}
