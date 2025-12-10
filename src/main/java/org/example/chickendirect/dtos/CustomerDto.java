package org.example.chickendirect.dtos;

import java.util.List;

public record CustomerDto(
        String name,
        String phoneNumber,
        String email,
        List<AddressDto> addressList
)
{
}
