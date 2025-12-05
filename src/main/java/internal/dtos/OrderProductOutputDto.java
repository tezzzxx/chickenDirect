package internal.dtos;

import java.math.BigDecimal;

public record OrderProductOutputDto(

        long orderProductId,
        long productId,
        String name,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
