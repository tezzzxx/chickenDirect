package org.example.chickendirect.dtos;

public record OrderProductInputDto(
        long productId,
        int quantity
){
}
