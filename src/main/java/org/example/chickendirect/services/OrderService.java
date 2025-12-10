package org.example.chickendirect.services;
import org.example.chickendirect.dtos.OrderInputDto;
import org.example.chickendirect.dtos.OrderOutputDto;
import org.example.chickendirect.dtos.OrderProductOutputDto;
import org.example.chickendirect.entities.*;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.repos.ProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepo orderRepo;
    private final CustomerRepo customerRepo;
    private final AddressRepo addressRepo;
    private final ProductRepo productRepo;

    private static final BigDecimal FREE_SHIPPING_LIMIT = new BigDecimal("600");
    private static final BigDecimal STANDARD_SHIPPING = new BigDecimal("150");

    public OrderService(OrderRepo orderRepo, CustomerRepo customerRepo, AddressRepo addressRepo, ProductRepo productRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.addressRepo = addressRepo;
        this.productRepo = productRepo;
    }

    public OrderOutputDto createOrder(OrderInputDto input){

        Customer customer = customerRepo.findById(input.customerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        Address address = addressRepo.findById(input.addressId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setAddress(address);
        order.setDate(LocalDate.now());
        order.setOrderStatus(OrderStatus.CONFIRMED);

        List<OrderProduct> orderProducts = input.productItems().stream()
                .map(item -> {
                    Product product = productRepo.findById(item.productId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Product not found with id" + item.productId()));

                    OrderProduct op = new OrderProduct();
                    op.setOrder(order);
                    op.setProduct(product);
                    op.setQuantity(item.quantity());
                    op.setUnitPrice(product.getPrice());
                    return op;
                })
                .toList();

        order.setItems(orderProducts);
        order.setTotalSum(calculateTotal(orderProducts));
        order.setShippingCharge(calculateShipping(orderProducts));

        Order savedOrder = orderRepo.save(order);
        return mapToDto(savedOrder);

    }

    public List<OrderOutputDto> findAllOrders(){
        return orderRepo.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public OrderOutputDto findOrderById(long id){
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id " + id));
        return mapToDto(order);
    }

    public void deleteOrderById(long id){
        if (!orderRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id " + id);
        }
        orderRepo.deleteById(id);
    }

    private OrderOutputDto mapToDto(Order order){
        List<OrderProductOutputDto> items = order.getItems().stream()
                .map(op -> new OrderProductOutputDto(
                        op.getOrderProductId(),
                        op.getProduct().getProductId(),
                        op.getProduct().getName(),
                        op.getQuantity(),
                        op.getUnitPrice(),
                        op.getUnitPrice().multiply(BigDecimal.valueOf(op.getQuantity()))
                ))
                .toList();

        return new OrderOutputDto(
                order.getOrderId(),
                order.getCustomer().getCustomerId(),
                order.getAddress().getAddressId(),
                order.getDate(),
                order.getTotalSum(),
                order.getShippingCharge(),
                order.getOrderStatus(),
                items
        );
    }

    private BigDecimal calculateTotal(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(op -> op.getUnitPrice().multiply(BigDecimal.valueOf(op.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateShipping(List<OrderProduct> orderProducts) {
        BigDecimal total = calculateTotal(orderProducts);

        if(total.compareTo(FREE_SHIPPING_LIMIT) > 0){
            return BigDecimal.ZERO;
        }
        return STANDARD_SHIPPING;
    }
}
