package org.example.chickendirect.dtos;

import java.math.BigDecimal;

public record OrderProductInputDto(
        long productId,
        int quantity
){
}
