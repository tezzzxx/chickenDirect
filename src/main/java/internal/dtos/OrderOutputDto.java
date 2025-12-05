package internal.dtos;

import internal.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderOutputDto(
        long orderId,
        long customerId,
        long addressId,
        LocalDate date,
        BigDecimal totalSum,
        BigDecimal shippingCharge,
        OrderStatus orderStatus,
        List<OrderProductOutputDto> orderItems
) {
}
