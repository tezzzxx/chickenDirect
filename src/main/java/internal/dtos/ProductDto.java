package internal.dtos;

import internal.enums.ProductStatus;

public record ProductDto(
        String name,
        String description,
        Double price,
        ProductStatus productStatus,
        Long quantity
) {
}
