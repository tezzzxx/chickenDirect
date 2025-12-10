package internal.dtos;

import internal.enums.ProductStatus;

import java.math.BigDecimal;

public record ProductDto(
        String name,
        String description,
        BigDecimal price,
        ProductStatus productStatus,
        int quantity
) {
}
