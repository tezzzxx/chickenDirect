package internal.dtos;

import java.util.List;

public record OrderInputDto(
        long customerId,
        long addressId,
        List<OrderProductInputDto> productItems
) {
}
