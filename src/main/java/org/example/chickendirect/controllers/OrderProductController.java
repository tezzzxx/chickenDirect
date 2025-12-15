package org.example.chickendirect.controllers;

import org.example.chickendirect.dtos.OrderProductForCustomerOutputDto;
import org.example.chickendirect.services.OrderProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orderProduct")
public class OrderProductController {

    private final OrderProductService orderProductService;

    public OrderProductController(OrderProductService orderProductService) {
        this.orderProductService = orderProductService;
    }

    @GetMapping("/{orderId}/products")
    public List<OrderProductForCustomerOutputDto> getOrderProducts(
            @PathVariable long orderId,
            @RequestParam String email
    ) {
        return orderProductService.getOrderProductsForCustomer(orderId, email);
    }
}
