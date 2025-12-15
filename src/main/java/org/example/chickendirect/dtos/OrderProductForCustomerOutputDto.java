package org.example.chickendirect.dtos;

import java.math.BigDecimal;

public record OrderProductForCustomerOutputDto (
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
){
}
