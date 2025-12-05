package internal.dtos;

import java.math.BigDecimal;

public record OrderProductInputDto(
        long productId,
        BigDecimal quantity
){
}
