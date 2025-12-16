package org.example.chickendirect.controllers;

import org.example.chickendirect.dtos.OrderProductForCustomerOutputDto;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.services.OrderProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orderProduct")
public class OrderProductController {

    private final OrderProductService orderProductService;

    public OrderProductController(OrderProductService orderProductService, OrderRepo orderRepo) {
        this.orderProductService = orderProductService;
    }

    @GetMapping("/{orderId}/products")
    public List<OrderProductForCustomerOutputDto> getOrderProducts(
            @PathVariable long orderId,
            @RequestParam String email
    ) {
        return orderProductService.getOrderProductsForCustomer(orderId, email);
    }

    @PutMapping("/{orderId}/products/updateOrder")
    public OrderProductForCustomerOutputDto updateOrderProductQuantity(
            @PathVariable long orderId,
            @RequestParam String productName,
            @RequestParam int newQuantity,
            @RequestParam String email
    ) {
        return orderProductService.updateOrderProductQuantity(orderId, productName, newQuantity, email);
    }

    @PostMapping("/{orderId}/productsAdd")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderProductForCustomerOutputDto addProductToOrder(
            @PathVariable long orderId,
            @RequestParam long productId,
            @RequestParam int quantity,
            @RequestParam String email
    ) {
        return orderProductService.addProductToOrder(orderId, productId, quantity, email);
    }

    @DeleteMapping("/{orderId}/productDelete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProductFromOrder(
            @PathVariable long orderId,
            @RequestParam long productId,
            @RequestParam String email
    ) {
        orderProductService.deleteProductFromOrder(orderId, productId, email);
    }
}
