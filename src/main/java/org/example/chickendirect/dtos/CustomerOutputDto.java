package org.example.chickendirect.dtos;

public record CustomerOutputDto(
        long customerId,
        String name,
        String phoneNumber,
        String email
) {
}
