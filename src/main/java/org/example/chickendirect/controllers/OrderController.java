package org.example.chickendirect.controllers;

import org.example.chickendirect.dtos.OrderInputDto;
import org.example.chickendirect.dtos.OrderOutputDto;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderOutputDto> createOrder(@RequestBody OrderInputDto input){
        log.info("Received request to create order: {}", input);

        OrderOutputDto order = orderService.createOrder(input);
        log.info("Order created successfully with id: {}", order.orderId());

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderOutputDto>> findAllOrders(){
        log.info("Received request to fetch all orders");

        List<OrderOutputDto> orders = orderService.findAllOrders();
        log.info("Returning {} orders", orders.size());

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderOutputDto> findOrderById(@PathVariable long orderId){
        log.info("Received request to fetch order with id {}", orderId);

        OrderOutputDto order = orderService.findOrderById(orderId);
        log.info("Order with id {} fetched successfully", orderId);

        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrderById(@PathVariable long orderId){
        log.info("Received request to delete order with id {}", orderId);

        orderService.deleteOrderById(orderId);
        log.info("Order with id {} deleted successfully", orderId);

        return ResponseEntity.ok("Order deleted");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderOutputDto> updateOrderStatus(
            @PathVariable long id,
            @RequestParam OrderStatus status) {
        log.info("Received request to update status of order {} to {}", id, status);

        OrderOutputDto updatedOrder = orderService.updateOrderStatus(id, status);
        log.info("Order {} status updated successfully to {}", id, status);

        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderOutputDto>> findOrdersByCustomerId(
            @PathVariable long customerId) {
        log.info("Received request to fetch orders for customer with id {}", customerId);

        List<OrderOutputDto> orders = orderService.findOrderByCustomerId(customerId);
        log.info("Returning {} orders for customer with id {}", orders.size(), customerId);

        return ResponseEntity.ok(orders);
    }

}
