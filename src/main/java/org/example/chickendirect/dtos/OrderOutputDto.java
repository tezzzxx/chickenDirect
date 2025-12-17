package org.example.chickendirect.dtos;

import org.example.chickendirect.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderOutputDto(
        long orderId,
        CustomerOutputDto customer,
        AddressOutputDto address,
        LocalDate date,
        BigDecimal totalSum,
        BigDecimal shippingCharge,
        OrderStatus orderStatus,
        List<OrderProductOutputDto> orderItems
) {
}
