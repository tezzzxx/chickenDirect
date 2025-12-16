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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderProductService {

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

        List<OrderProduct> orderProducts = orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(orderId, customerEmail);
        if (orderProducts.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No order was found with this id");
        }

        OrderProduct orderProduct = orderProducts.stream()
                .filter(op -> op.getProduct().getName().equalsIgnoreCase(productName))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product name does not exist"));

        if (orderProduct.getOrder().getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order cannot be updated because its status is " + orderProduct.getOrder().getOrderStatus());
        }

        Product product = productRepo.findByName(productName)
                .orElseThrow(() ->new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in the database"));

        int oldQuantity = orderProduct.getQuantity();
        int updatedQuantity = newQuantity - oldQuantity;

        if(newQuantity == oldQuantity){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have to update the quantity");
        }

        if (product.getQuantity() < updatedQuantity){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough in stock for this change");
        }

        product.setQuantity(product.getQuantity() - updatedQuantity);
        orderProduct.setQuantity(newQuantity);

        productRepo.save(product);
        orderProductRepo.save(orderProduct);

        Order order = orderProduct.getOrder();
        BigDecimal totalsum = order.getItems().stream()
                .map(op -> op.getUnitPrice().multiply(BigDecimal.valueOf(op.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalSum(totalsum);
        orderRepo.save(order);

        return mapToDto(orderProduct);
    }

    public OrderProductForCustomerOutputDto addProductToOrder(long orderId, long productId, int quantity, String customerEmail){

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order with this id was not found"));

        if (!order.getCustomer().getEmail().equalsIgnoreCase(customerEmail)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cant modify this order");
        }

        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot add products. Order status is " + order.getOrderStatus());
        }

        boolean productExists = order.getItems().stream()
                .anyMatch(op -> op.getProduct().getProductId() == productId);
        if (productExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product already exists in the order");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException (HttpStatus.NOT_FOUND, "Product with this id was not found"));

        if (product.getQuantity() < quantity){
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

        return mapToDto(newOrderProduct);
    }

    public List<OrderProductForCustomerOutputDto> findAllOrderProducts(){
        return orderProductRepo.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public OrderProductForCustomerOutputDto findOrderProductById(long id){
        OrderProduct orderProduct = orderProductRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order-product not found with id " + id));
        return mapToDto(orderProduct);
    }

    public List<OrderProductForCustomerOutputDto> getOrderProductsForCustomer(long orderId, String email) {
        List<OrderProduct> orderProducts = orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(orderId, email);
        if (orderProducts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this order");
        }
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
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getCustomer().getEmail().equalsIgnoreCase(customerEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot modify this order");
        }

        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete products. Order status is " + order.getOrderStatus());
        }

        OrderProduct orderProduct = order.getItems().stream()
                .filter(op -> op.getProduct().getProductId() == productId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in order"));

        Product product = orderProduct.getProduct();
        product.setQuantity(product.getQuantity() + orderProduct.getQuantity());
        productRepo.save(product);

        order.getItems().remove(orderProduct);
        orderProductRepo.delete(orderProduct);

        BigDecimal totalsum = order.getItems().stream()
                .map(op -> op.getUnitPrice().multiply(BigDecimal.valueOf(op.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalSum(totalsum);
        orderRepo.save(order);
    }
}
