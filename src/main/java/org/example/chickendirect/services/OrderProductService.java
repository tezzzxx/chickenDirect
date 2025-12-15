package org.example.chickendirect.services;

import org.example.chickendirect.dtos.OrderOutputDto;
import org.example.chickendirect.dtos.OrderProductForCustomerOutputDto;
import org.example.chickendirect.entities.OrderProduct;
import org.example.chickendirect.repos.OrderProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderProductService {

    private final OrderProductRepo orderProductRepo;

    public OrderProductService(OrderProductRepo orderProductRepo) {
        this.orderProductRepo = orderProductRepo;
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
}
