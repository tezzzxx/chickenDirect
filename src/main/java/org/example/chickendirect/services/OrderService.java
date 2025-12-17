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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepo orderRepo;
    private final CustomerRepo customerRepo;
    private final AddressRepo addressRepo;
    private final ProductRepo productRepo;

    private static final BigDecimal FREE_SHIPPING_LIMIT = new BigDecimal("600");
    private static final BigDecimal STANDARD_SHIPPING = new BigDecimal("150");

    public static final int LOW_STOCK_THRESHOLD = 10;

    public static int getLowStockThreshold() {
        return LOW_STOCK_THRESHOLD;
    }

    public OrderService(OrderRepo orderRepo, CustomerRepo customerRepo, AddressRepo addressRepo, ProductRepo productRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.addressRepo = addressRepo;
        this.productRepo = productRepo;
    }

    @Transactional
    public OrderOutputDto createOrder(OrderInputDto input) {
        log.info("Creating order for customerId: {}, addressId: {}", input.customerId(), input.addressId());

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
        log.info("Order created successfully with orderId: {}", savedOrder.getOrderId());
        return mapToDto(savedOrder);
    }

    private Customer fetchCustomer(Long customerId){
        log.info("Fetching customer with id: {}", customerId);
        return customerRepo.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id: {}", customerId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with id " + customerId);
                });
    }

    private Address fetchAddress(Long addressId){
        log.info("Fetching address with id: {}", addressId);
        return addressRepo.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address not found with id: {}", addressId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");
                });
    }

    private OrderProduct processOrderProduct(Order order, OrderProductInputDto item){
        log.info("Processing order item for productId: {} with quantity: {}", item.productId(), item.quantity());

        Product product = productRepo.findByIdForUpdate(item.productId())
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", item.productId());
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found with id" + item.productId());
                });

        int stockQuantity = product.getQuantity();
        int orderedQuantity = item.quantity();

        if (stockQuantity <= 0){
            log.warn("Product '{}' is out of stock", product.getName());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product '" + product.getName() + "' is out of stock"
            );
        }

        if(orderedQuantity > stockQuantity ){
            log.warn("Product '{}' has insufficient stock. Requested: {}, Available: {}", product.getName(), orderedQuantity, stockQuantity);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product " + product.getName() + " only has " + stockQuantity + "kg in stock ");
        }

        int remaining = stockQuantity - orderedQuantity;
        product.setQuantity(remaining);
        updateProductStatus(product, remaining);

        log.info("Product '{}' stock updated. Remaining quantity: {}", product.getName(), remaining);

        OrderProduct op = new OrderProduct();
        op.setOrder(order);
        op.setProduct(product);
        op.setQuantity(item.quantity());
        op.setUnitPrice(product.getPrice());
        return op;
    }

    void updateProductStatus(Product product, int remainingQuantity) {
        ProductStatus newStatus;
        if (remainingQuantity == 0){
            newStatus = ProductStatus.OUT_OF_STOCK;
            log.warn("Product '{}' is now out of stock", product.getName());
        } else if (remainingQuantity <= LOW_STOCK_THRESHOLD){
            newStatus = ProductStatus.PENDING_RESTOCK;
            log.warn("Product '{}' is low in stock. Remaining quantity: {}", product.getName(), remainingQuantity);
        } else {
            newStatus = ProductStatus.IN_STOCK;
            log.info("Product '{}' is in stock. Remaining quantity: {}", product.getName(), remainingQuantity);
        }

        if(product.getProductStatus() != newStatus){
            product.setProductStatus(newStatus);
        }
    }

    public OrderOutputDto updateOrderStatus(long orderId, OrderStatus newStatus){
        log.info("Updating status for orderId: {} to {}", orderId, newStatus);
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() ->{
                    log.warn("No order found with id: {}", orderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "No order was found with this id");
                });

        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepo.save(order);
        log.info("OrderId: {} status updated successfully", orderId);
        return mapToDto(updatedOrder);
    }

    public List<OrderOutputDto> findAllOrders(){
        log.info("Fetching all orders");
        return orderRepo.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public OrderOutputDto findOrderById(long id){
        log.info("Fetching order by id: {}", id);
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order not found with id: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Order not found with id " + id);
                });
        return mapToDto(order);
    }

    public List<OrderOutputDto> findOrderByCustomerId(long customerId){
        log.info("Fetching orders for customerId: {}", customerId);
        if (!customerRepo.existsById(customerId)) {
            log.warn("Customer not found with id: {}", customerId);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Customer not found with id " + customerId
            );
        }

        List<Order> orders = orderRepo.findByCustomerCustomerId(customerId);

        if (orders.isEmpty()) {
            log.warn("No orders found for customerId: {}", customerId);
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
        log.info("Deleting order with id: {}", id);
        if (!orderRepo.existsById(id)) {
            log.warn("Order not found with id: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id " + id);
        }
        orderRepo.deleteById(id);
        log.info("Order with id: {} deleted successfully", id);
    }

    private OrderOutputDto mapToDto(Order order){
        CustomerOutputDto customerDto = new CustomerOutputDto(
                order.getCustomer().getCustomerId(),
                order.getCustomer().getName(),
                order.getCustomer().getPhoneNumber(),
                order.getCustomer().getEmail()
        );

        AddressOutputDto addressDto = new AddressOutputDto(
                order.getAddress().getAddressId(),
                order.getAddress().getApartmentNumber(),
                order.getAddress().getAddress(),
                order.getAddress().getZipCode(),
                order.getAddress().getCity(),
                order.getAddress().getCountry()
        );

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
                customerDto,
                addressDto,
                order.getDate(),
                order.getTotalSum(),
                order.getShippingCharge(),
                order.getOrderStatus(),
                items
        );
    }

    private BigDecimal calculateTotal(List<OrderProduct> orderProducts) {
        BigDecimal total = orderProducts.stream()
                .map(op -> op.getUnitPrice().multiply(BigDecimal.valueOf(op.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Calculated total order amount: {}", total);
        return total;
    }

    private BigDecimal calculateShipping(List<OrderProduct> orderProducts) {
        BigDecimal total = calculateTotal(orderProducts);

        if(total.compareTo(FREE_SHIPPING_LIMIT) > 0){
            log.info("Order qualifies for free shipping. Total: {}", total);
            return BigDecimal.ZERO;
        }
        log.info("Standard shipping charge applied: {}", STANDARD_SHIPPING);
        return STANDARD_SHIPPING;
    }
}