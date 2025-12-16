package org.example.chickendirect.controllers;

import org.example.chickendirect.dtos.OrderInputDto;
import org.example.chickendirect.dtos.OrderOutputDto;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderOutputDto> createOrder(@RequestBody OrderInputDto input){
        OrderOutputDto order = orderService.createOrder(input);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderOutputDto>> findAllOrders(){
        return ResponseEntity.ok(orderService.findAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderOutputDto> findOrderById(@PathVariable long orderId){
        return ResponseEntity.ok(orderService.findOrderById(orderId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrderById(@PathVariable long orderId){
        orderService.deleteOrderById(orderId);
        return ResponseEntity.ok("Order deleted");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderOutputDto> updateOrderStatus(
            @PathVariable long id,
            @RequestParam OrderStatus status) {
        OrderOutputDto updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderOutputDto>> findOrdersByCustomerId(
            @PathVariable long customerId) {

        return ResponseEntity.ok(
                orderService.findOrderByCustomerId(customerId)
        );
    }

}
