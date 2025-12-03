package internal.dtos;

import internal.entities.Address;

import java.util.List;

public record CustomerDto(
        String name,
        String phoneNumber,
        String email,
        List<Address> addressList
)
{
}
