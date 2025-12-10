package internal.dtos;

import java.math.BigDecimal;

public record OrderProductOutputDto(

        long orderProductId,
        long productId,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
