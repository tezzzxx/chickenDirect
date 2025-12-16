package org.example.chickendirect.controllers;

import org.example.chickendirect.dtos.OrderProductForCustomerOutputDto;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.services.OrderProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orderProduct")
public class OrderProductController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final OrderProductService orderProductService;

    public OrderProductController(OrderProductService orderProductService, OrderRepo orderRepo) {
        this.orderProductService = orderProductService;
    }

    @GetMapping("/{orderId}/products")
    public List<OrderProductForCustomerOutputDto> getOrderProducts(
            @PathVariable long orderId,
            @RequestParam String email
    ) {
        log.info("Fetching all products for orderId={} and customerEmail={}", orderId, email);
        List<OrderProductForCustomerOutputDto> result = orderProductService.getOrderProductsForCustomer(orderId, email);
        log.info("Fetched {} products for orderId={} and customerEmail={}", result.size(), orderId, email);
        return result;
    }

    @PutMapping("/{orderId}/products/updateOrder")
    public OrderProductForCustomerOutputDto updateOrderProductQuantity(
            @PathVariable long orderId,
            @RequestParam String productName,
            @RequestParam int newQuantity,
            @RequestParam String email
    ) {
        log.info("Updating product '{}' in orderId={} for customerEmail={} to new quantity={}",
                productName, orderId, email, newQuantity);
        OrderProductForCustomerOutputDto result = orderProductService.updateOrderProductQuantity(orderId, productName, newQuantity, email);
        log.info("Updated product '{}' in orderId={} successfully", productName, orderId);
        return result;
    }

    @PostMapping("/{orderId}/productsAdd")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderProductForCustomerOutputDto addProductToOrder(
            @PathVariable long orderId,
            @RequestParam long productId,
            @RequestParam int quantity,
            @RequestParam String email
    ) {
        log.info("Adding productId={} with quantity={} to orderId={} for customerEmail={}", productId, quantity, orderId, email);
        OrderProductForCustomerOutputDto result = orderProductService.addProductToOrder(orderId, productId, quantity, email);
        log.info("ProductId={} added to orderId={} successfully", productId, orderId);
        return result;
    }

    @DeleteMapping("/{orderId}/productDelete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProductFromOrder(
            @PathVariable long orderId,
            @RequestParam long productId,
            @RequestParam String email
    ) {
        log.info("Deleting productId={} from orderId={} for customerEmail={}", productId, orderId, email);
        orderProductService.deleteProductFromOrder(orderId, productId, email);
        log.info("Deleted productId={} from orderId={} successfully", productId, orderId);
    }

    @GetMapping("/findAll")
    public List<OrderProductForCustomerOutputDto> getAllOrderProducts() {
        log.info("Fetching all order products");
        List<OrderProductForCustomerOutputDto> result = orderProductService.findAllOrderProducts();
        log.info("Fetched {} order products", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public OrderProductForCustomerOutputDto getOrderProductById(@PathVariable long id) {
        log.info("Fetching order product by id={}", id);
        OrderProductForCustomerOutputDto result = orderProductService.findOrderProductById(id);
        log.info("Fetched order product with id={}", id);
        return result;
    }
}
