package org.example.chickendirect.dtos;

import java.util.List;

public record OrderInputDto(
        long customerId,
        long addressId,
        List<OrderProductInputDto> productItems
) {
}
