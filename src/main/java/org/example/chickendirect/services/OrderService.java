package org.example.chickendirect.services;
import org.example.chickendirect.dtos.*;
import org.example.chickendirect.enums.ProductStatus;
import org.springframework.transaction.annotation.Transactional;
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

    private static final int LOW_STOCK_THRESHOLD = 10;

    public OrderService(OrderRepo orderRepo, CustomerRepo customerRepo, AddressRepo addressRepo, ProductRepo productRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.addressRepo = addressRepo;
        this.productRepo = productRepo;
    }

    @Transactional
    public OrderOutputDto createOrder(OrderInputDto input) {

        Customer customer = fetchCustomer(input.customerId());
        Address address = fetchAddress(input.addressId());

        Order order = new Order();
        order.setCustomer(customer);
        order.setAddress(address);
        order.setDate(LocalDate.now());
        order.setOrderStatus(OrderStatus.CONFIRMED);

        List<OrderProduct> orderProducts = input.productItems().stream()
                .map(item -> processOrderProduct(order, item))
                .toList();

        order.setItems(orderProducts);
        order.setTotalSum(calculateTotal(orderProducts));
        order.setShippingCharge(calculateShipping(orderProducts));

        Order savedOrder = orderRepo.save(order);
        return mapToDto(savedOrder);
    }

    private Customer fetchCustomer(Long customerId){
        return customerRepo.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with id " + customerId));
    }

    private Address fetchAddress(Long addressId){
        return addressRepo.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
    }

    private OrderProduct processOrderProduct(Order order, OrderProductInputDto item){

        Product product = productRepo.findByIdForUpdate(item.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id" + item.productId()));

        int stockQuantity = product.getQuantity();
        int orderedQuantity = item.quantity();

        if (stockQuantity <= 0){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product '" + product.getName() + "' is out of stock"
            );
        }

        if(orderedQuantity > stockQuantity ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product " + product.getName() + "only has " + stockQuantity + "kg in stock ");
        }

        int remaining = stockQuantity - orderedQuantity;

        product.setQuantity(remaining);
        updateProductStatus(product, remaining);
        
        OrderProduct op = new OrderProduct();
        op.setOrder(order);
        op.setProduct(product);
        op.setQuantity(item.quantity());
        op.setUnitPrice(product.getPrice());
        return op;

    }

    private void updateProductStatus(Product product, int remainingQuantity) {

       ProductStatus newStatus;
        if (remainingQuantity == 0){
            newStatus = ProductStatus.OUT_OF_STOCK;
        } else if (remainingQuantity <= LOW_STOCK_THRESHOLD){
            newStatus = ProductStatus.PENDING_RESTOCK;
        } else {
            newStatus = ProductStatus.IN_STOCK;
        }

        if(product.getProductStatus() != newStatus){
            product.setProductStatus(newStatus);
        }
    }


    public OrderOutputDto updateOrderStatus(long orderId, OrderStatus newStatus){
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() ->new ResponseStatusException(HttpStatus.NOT_FOUND, "No order was found with this id"));

        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepo.save(order);
        return mapToDto(updatedOrder);
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

    public List<OrderOutputDto> findOrderByCustomerId(long customerId){
        if (!customerRepo.existsById(customerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Customer not found with id " + customerId
            );
        }

        List<Order> orders = orderRepo.findByCustomerCustomerId(customerId);

        if (orders.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No orders found for customer with id " + customerId
            );
        }

        return orders.stream()
                .map(this::mapToDto)
                .toList();
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
