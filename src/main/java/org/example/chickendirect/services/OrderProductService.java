package org.example.chickendirect.services;

import jakarta.transaction.Transactional;
import org.example.chickendirect.dtos.OrderProductForCustomerOutputDto;
import org.example.chickendirect.entities.Order;
import org.example.chickendirect.entities.OrderProduct;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.repos.OrderProductRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.repos.ProductRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderProductService {

    private static final Logger log = LoggerFactory.getLogger(OrderProductService.class);
    private final OrderProductRepo orderProductRepo;
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;

    public OrderProductService(OrderProductRepo orderProductRepo, ProductRepo productRepo, OrderRepo orderRepo) {
        this.orderProductRepo = orderProductRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    @Transactional
    public OrderProductForCustomerOutputDto updateOrderProductQuantity(
            long orderId, String productName, int newQuantity, String customerEmail) {

        log.info("Updating orderId={} for customerEmail={} - setting quantity for productName={} to newQuantity={}",
                orderId, customerEmail, productName, newQuantity);

        List<OrderProduct> orderProducts = orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(orderId, customerEmail);
        if (orderProducts.isEmpty()) {
            log.warn("No order found for orderId={} and customerEmail={}", orderId, customerEmail);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No order was found with this id");
        }

        OrderProduct orderProduct = orderProducts.stream()
                .filter(op -> op.getProduct().getName().equalsIgnoreCase(productName))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Product '{}' not found in orderId={}", productName, orderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product name does not exist in this order");
                });

        if (orderProduct.getOrder().getOrderStatus() != OrderStatus.CONFIRMED) {
            log.warn("Cannot update quantity: Order status={} blocks update", orderProduct.getOrder().getOrderStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order cannot be updated because its status is " + orderProduct.getOrder().getOrderStatus());
        }

        Product product = productRepo.findByName(productName)
                .orElseThrow(() -> {
                    log.warn("Product '{}' not found in database", productName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in the database");
                });

        int oldQuantity = orderProduct.getQuantity();
        int updatedQuantity = newQuantity - oldQuantity;

        if (newQuantity <= 0) {
            log.warn("Invalid newQuantity={} for productName={}", newQuantity, productName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        if (newQuantity == oldQuantity) {
            log.warn("Quantity not changed for productName={}, must provide a different value", productName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have to update the quantity");
        }

        if (updatedQuantity > product.getQuantity()) {
            log.warn("Not enough stock for productName={} (requested={}, available={})",
                    productName, updatedQuantity, product.getQuantity());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for this change");
        }

        product.setQuantity(product.getQuantity() - updatedQuantity);
        orderProduct.setQuantity(newQuantity);
        log.info("Updated quantity for productName={} to newQuantity={}", productName, newQuantity);

        productRepo.save(product);
        orderProductRepo.save(orderProduct);

        Order order = orderProduct.getOrder();
        BigDecimal totalSum = order.getItems().stream()
                .map(op -> op.getUnitPrice().multiply(BigDecimal.valueOf(op.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalSum(totalSum);
        log.info("Updated totalsum for orderId={} to {}", order.getOrderId(), totalSum);

        orderRepo.save(order);

        return mapToDto(orderProduct);
    }

    public OrderProductForCustomerOutputDto addProductToOrder(long orderId, long productId, int quantity, String customerEmail){

        log.info("Adding productId={} with quantity={} to orderId={} for customerEmail={}",
                productId, quantity, orderId, customerEmail);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order with orderId={} not found", orderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Order with this id was not found");
                });

        if (!order.getCustomer().getEmail().equalsIgnoreCase(customerEmail)) {
            log.warn("CustomerEmail={} tried to modify orderId={} which they don't own", customerEmail, orderId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cant modify this order");
        }

        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            log.warn("Cannot add products to orderId={} because its status is {}", orderId, order.getOrderStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot add products. Order status is " + order.getOrderStatus());
        }

        boolean productExists = order.getItems().stream()
                .anyMatch(op -> op.getProduct().getProductId() == productId);
        if (productExists) {
            log.warn("Product with productId={} already exists in orderId={}", productId, orderId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product already exists in the order");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product with productId={} not found", productId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with this id was not found");
                });

        if (product.getQuantity() < quantity) {
            log.warn("Not enough stock for product '{}' (requested={}, available={})",
                    product.getName(), quantity, product.getQuantity());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Not enough stock for product '" + product.getName() + "'");
        }

        OrderProduct newOrderProduct = new OrderProduct();
        newOrderProduct.setOrder(order);
        newOrderProduct.setProduct(product);
        newOrderProduct.setQuantity(quantity);
        newOrderProduct.setUnitPrice(product.getPrice());

        product.setQuantity(product.getQuantity() - quantity);
        productRepo.save(product);
        orderProductRepo.save(newOrderProduct);

        order.getItems().add(newOrderProduct);
        BigDecimal totalsum = order.getItems().stream()
                .map(op -> op.getUnitPrice().multiply(BigDecimal.valueOf(op.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalSum(totalsum);
        orderRepo.save(order);

        log.info("Product '{}' added to orderId={} successfully. New totalsum={}", product.getName(), orderId, totalsum);


        return mapToDto(newOrderProduct);
    }

    public List<OrderProductForCustomerOutputDto> findAllOrderProducts() {
        log.info("Fetching all order products from the database");

        List<OrderProductForCustomerOutputDto> result = orderProductRepo.findAll().stream()
                .map(this::mapToDto)
                .toList();

        log.info("Fetched {} order products", result.size());
        return result;
    }

    public OrderProductForCustomerOutputDto findOrderProductById(long id) {
        log.info("Fetching order product with id={}", id);

        OrderProduct orderProduct = orderProductRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order-product with id={} not found", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Order-product not found with id " + id);
                });

        log.info("Order-product with id={} fetched successfully", id);
        return mapToDto(orderProduct);
    }

    public List<OrderProductForCustomerOutputDto> getOrderProductsForCustomer(long orderId, String email) {
        log.info("Fetching order products for orderId={} and customerEmail={}", orderId, email);

        List<OrderProduct> orderProducts = orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(orderId, email);

        if (orderProducts.isEmpty()) {
            log.warn("No order products found or access denied for orderId={} and customerEmail={}", orderId, email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this order");
        }

        log.info("Fetched {} order products for orderId={} and customerEmail={}", orderProducts.size(), orderId, email);
        return orderProducts.stream().map(this::mapToDto).toList();
    }

    private OrderProductForCustomerOutputDto mapToDto(OrderProduct op) {
        return new OrderProductForCustomerOutputDto(
                op.getProduct().getName(),
                op.getQuantity(),
                op.getUnitPrice(),
                op.getUnitPrice().multiply(BigDecimal.valueOf(op.getQuantity()))
        );
    }

    @Transactional
    public void deleteProductFromOrder(long orderId, long productId, String customerEmail) {
        log.info("Attempting to delete productId={} from orderId={} for customerEmail={}", productId, orderId, customerEmail);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order with orderId={} not found", orderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
                });

        if (!order.getCustomer().getEmail().equalsIgnoreCase(customerEmail)) {
            log.warn("CustomerEmail={} tried to modify orderId={} which they do not own", customerEmail, orderId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot modify this order");
        }

        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            log.warn("Cannot delete products from orderId={} because its status is {}", orderId, order.getOrderStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete products. Order status is " + order.getOrderStatus());
        }

        OrderProduct orderProduct = order.getItems().stream()
                .filter(op -> op.getProduct().getProductId() == productId)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Product with productId={} not found in orderId={}", productId, orderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in order");
                });

        Product product = orderProduct.getProduct();
        product.setQuantity(product.getQuantity() + orderProduct.getQuantity());
        productRepo.save(product);
        log.info("Restored {} units to productId={} stock", orderProduct.getQuantity(), productId);

        order.getItems().remove(orderProduct);
        orderProductRepo.delete(orderProduct);
        log.info("Deleted productId={} from orderId={}", productId, orderId);

        BigDecimal totalsum = order.getItems().stream()
                .map(op -> op.getUnitPrice().multiply(BigDecimal.valueOf(op.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalSum(totalsum);
        orderRepo.save(order);
        log.info("Updated totalsum for orderId={} to {}", orderId, totalsum);
    }
}
