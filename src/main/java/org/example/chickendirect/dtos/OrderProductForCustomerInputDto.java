package org.example.chickendirect.dtos;

public record OrderProductForCustomerInputDto(
        String email,
        long orderId
) {
}
