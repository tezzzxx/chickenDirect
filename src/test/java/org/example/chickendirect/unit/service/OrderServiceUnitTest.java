package org.example.chickendirect.unit.service;

import org.example.chickendirect.dtos.OrderInputDto;
import org.example.chickendirect.dtos.OrderOutputDto;
import org.example.chickendirect.dtos.OrderProductInputDto;
import org.example.chickendirect.entities.*;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.repos.ProductRepo;
import org.example.chickendirect.services.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private AddressRepo addressRepo;

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private OrderService orderService;

    @Nested
    class CreateOrderTests {

        @Test
        void testCreateOrder_success() {

            Customer customer = new Customer();
            customer.setCustomerId(1L);

            Address address = new Address();
            address.setAddressId(1L);

            Product product = new Product();
            product.setProductId(1L);
            product.setName("Chicken Wings");
            product.setPrice(BigDecimal.TEN);
            product.setQuantity(20);
            product.setProductStatus(ProductStatus.IN_STOCK);

            OrderProductInputDto item =
                    new OrderProductInputDto(1L, 5);

            OrderInputDto input = new OrderInputDto(
                    1L, 1L, List.of(item)
            );

            when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
            when(addressRepo.findById(1L)).thenReturn(Optional.of(address));
            when(productRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
            when(orderRepo.save(Mockito.any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            OrderOutputDto result = orderService.createOrder(input);

            assertNotNull(result);
            assertEquals(OrderStatus.CONFIRMED, result.orderStatus());
            assertEquals(BigDecimal.valueOf(50), result.totalSum());
            assertEquals(BigDecimal.valueOf(150), result.shippingCharge());
            assertEquals(1, result.orderItems().size());
            assertEquals(15, product.getQuantity());

            verify(customerRepo).findById(1L);
            verify(addressRepo).findById(1L);
            verify(productRepo).findByIdForUpdate(1L);
            verify(orderRepo).save(any(Order.class));
        }

        @Test
        void testCreateOrder_customerNotFound() {
            when(customerRepo.findById(1L)).thenReturn(Optional.empty());

            var input = new OrderInputDto(1L, 1L, List.of());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.createOrder(input));
            assertEquals(404, ex.getStatusCode().value());

        }

        @Test
        void testCreateOrder_addressNotFound() {
            when(customerRepo.findById(1L)).thenReturn(Optional.of(new Customer()));
            when(addressRepo.findById(1L)).thenReturn(Optional.empty());

            var input = new OrderInputDto(1L, 1L, List.of());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.createOrder(input));
            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        void testCreateOrder_productNotFound() {
            when(customerRepo.findById(anyLong())).thenReturn(Optional.of(new Customer()));
            when(addressRepo.findById(anyLong())).thenReturn(Optional.of(new Address()));
            when(productRepo.findByIdForUpdate(1L)).thenReturn(Optional.empty());

            var input = new OrderInputDto(1L, 1L, List.of(new OrderProductInputDto(1L, 1)));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.createOrder(input));
            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        void testCreateOrder_productOutOfStock() {
            Product product = new Product();
            product.setQuantity(0);
            product.setName("Chicken Wings");

            when(customerRepo.findById(anyLong())).thenReturn(Optional.of(new Customer()));
            when(addressRepo.findById(anyLong())).thenReturn(Optional.of(new Address()));
            when(productRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

            var input = new OrderInputDto(1L, 1L, List.of(new OrderProductInputDto(1L, 1)));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.createOrder(input));
            assertEquals(400, ex.getStatusCode().value());
        }

        @Test
        void testCreateOrder_NotEnoughInStock() {
            Product product = new Product();
            product.setQuantity(2);
            product.setName("Chicken");

            when(customerRepo.findById(anyLong())).thenReturn(Optional.of(new Customer()));
            when(addressRepo.findById(anyLong())).thenReturn(Optional.of(new Address()));
            when(productRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

            var input = new OrderInputDto(1L, 1L, List.of(new OrderProductInputDto(1L, 5)));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.createOrder(input));
            assertEquals(400, ex.getStatusCode().value());
        }

        @Test
        void testCreateOrder_shippingFree() {
            Product product = new Product();
            product.setQuantity(100);
            product.setPrice(BigDecimal.valueOf(700));

            when(customerRepo.findById(anyLong())).thenReturn(Optional.of(new Customer()));
            when(addressRepo.findById(anyLong())).thenReturn(Optional.of(new Address()));
            when(productRepo.findByIdForUpdate(anyLong())).thenReturn(Optional.of(product));
            when(orderRepo.save(any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var input = new OrderInputDto(1L, 1L, List.of(new OrderProductInputDto(1L, 1)));
            var result = orderService.createOrder(input);

            assertEquals(BigDecimal.ZERO, result.shippingCharge());
            assertEquals(99, product.getQuantity());
            assertEquals(ProductStatus.IN_STOCK, product.getProductStatus());
        }
    }

    @Nested
    class UpdateProductStatusTests{

        @Test
        void testStatusOutOfStock(){
            Product product = new Product();
            product.setProductStatus(ProductStatus.IN_STOCK);

            orderService.updateProductStatusByQuantity(product, 0);
            assertEquals(ProductStatus.OUT_OF_STOCK, product.getProductStatus());
        }

        @Test
        void testStatusPendingRestock(){
            Product product = new Product();
            product.setProductStatus(ProductStatus.IN_STOCK);

            orderService.updateProductStatusByQuantity(product, 5);
            assertEquals(ProductStatus.PENDING_RESTOCK, product.getProductStatus());
        }

        @Test
        void testStatusInStock(){
            Product product = new Product();
            product.setProductStatus(ProductStatus.OUT_OF_STOCK);

            orderService.updateProductStatusByQuantity(product, 20);
            assertEquals(ProductStatus.IN_STOCK, product.getProductStatus());
        }
    }

    @Nested
    class UpdateOrderStatusTests{

        @Test
        void testUpdateOrderStatus_success(){

            Customer customer = new Customer();
            customer.setCustomerId(1L);

            Address address = new Address();
            address.setAddressId(1L);

            Order order = new Order();
            order.setOrderId(1L);
            order.setOrderStatus(OrderStatus.DELIVERED);
            order.setCustomer(customer);
            order.setAddress(address);

            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepo.save(any(Order.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var result = orderService.updateOrderStatus(1L, OrderStatus.DELIVERED);
            assertEquals(OrderStatus.DELIVERED, result.orderStatus());

        }

        @Test
        void testUpdateOrderStatus_notFound(){
            when(orderRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED));

            assertEquals(404, ex.getStatusCode().value());
        }
    }

    @Nested
    class FindOrderByIdTests{

        @Test
        void testFindOrderById_success(){
            Customer customer = new Customer();
            customer.setCustomerId(1L);

            Address address = new Address();
            address.setAddressId(1L);

            Order order = new Order();
            order.setCustomer(customer);
            order.setAddress(address);

            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

            var result = orderService.findOrderById(1L);

            assertNotNull(result);
        }

        @Test
        void testFindOrderById_notFound(){
            when(orderRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.findOrderById(1L));
            assertEquals(404, ex.getStatusCode().value());
        }
    }

    @Nested
    class FindOrdersByCustomerIdTests{

        @Test
        void testFindOrdersByCustomerId_success(){
            when(customerRepo.existsById(1L)).thenReturn(true);

            Customer customer = new Customer();
            customer.setCustomerId(1L);
            customer.setName("Hannah Sval");
            customer.setEmail("HannahS@example.com");
            customer.setPhoneNumber("43445678");

            Address address = new Address();
            address.setAddressId(1L);

            Order order = new Order();
            order.setCustomer(customer);
            order.setAddress(address);

            when(orderRepo.findByCustomerCustomerId(1L)).thenReturn(List.of(order));

            var result = orderService.findOrderByCustomerId(1L);

            assertNotNull(result);
            assertFalse(result.isEmpty());

        }

        @Test
        void testFindOrdersByCustomer_customerNotFound(){
            when(customerRepo.existsById(1L)).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.findOrderByCustomerId(1L));
            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        void testFindOrdersByCustomerId_noOrders(){
            when(customerRepo.existsById(1L)).thenReturn(true);
            when(orderRepo.findByCustomerCustomerId(1L)).thenReturn(List.of());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.findOrderByCustomerId(1L));
            assertEquals(404, ex.getStatusCode().value());
        }
    }

    @Nested
    class DeleteOrderTests{

        @Test
        void testDeleteOrder_success_restoresStock(){
            Product product = new Product();
            product.setQuantity(5);
            product.setProductStatus(ProductStatus.PENDING_RESTOCK);

            OrderProduct op = new OrderProduct();
            op.setProduct(product);
            op.setQuantity(5);

            Order order = new Order();
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setItems(List.of(op));

            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
            when(productRepo.save(any(Product.class)))
                    .thenAnswer(i -> i.getArgument(0));

            orderService.deleteOrderById(1L);

            assertEquals(10, product.getQuantity());
            verify(orderRepo).delete(order);
        }

        @Test
        void testDeleteOrder_notFound(){
            when(orderRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.deleteOrderById(1L));
            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        void testDeleteOrder_notConfirmed(){
            Order order = new Order();
            order.setOrderStatus(OrderStatus.DELIVERED);

            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderService.deleteOrderById(1L));
            assertEquals(400, ex.getStatusCode().value());
        }

    }

}
