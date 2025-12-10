package org.example.chickendirect.dtos;

import org.example.chickendirect.enums.ProductStatus;

import java.math.BigDecimal;

public record ProductDto(
        String name,
        String description,
        BigDecimal price,
        ProductStatus productStatus,
        int quantity,
        String unit
) {
}
