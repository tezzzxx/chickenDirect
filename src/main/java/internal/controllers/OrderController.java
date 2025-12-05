package internal.controllers;

import internal.dtos.OrderInputDto;
import internal.dtos.OrderOutputDto;
import internal.services.OrderService;
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
        OrderOutputDto orderOutputDto = orderService.createOrder(input);
        return new ResponseEntity<>(orderOutputDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderOutputDto>> findAllOrders(){
        return ResponseEntity.ok(orderService.findAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderOutputDto> findOrderById(@PathVariable long id){
        return ResponseEntity.ok(orderService.findOrderById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrderById(@PathVariable long id){
        orderService.deleteOrderById(id);
        return ResponseEntity.ok("Order deleted");
    }

}
