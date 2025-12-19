package org.example.chickendirect.unit.service;

import org.example.chickendirect.dtos.OrderProductForCustomerOutputDto;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.entities.Order;
import org.example.chickendirect.entities.OrderProduct;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.repos.OrderProductRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.repos.ProductRepo;
import org.example.chickendirect.services.OrderProductService;
import org.example.chickendirect.services.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderProductServiceUnitTest {
    @Mock
    private OrderProductRepo orderProductRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderProductService orderProductService;

    @Nested
    class UpdateOrderProductQuantityTests {

        @Test
        void testUpdateOrderProductQuantity_success() {
            Product product = new Product();
            product.setName("Chicken Wings");
            product.setQuantity(10);
            product.setPrice(BigDecimal.valueOf(5));

            Order order = new Order();
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setItems(new ArrayList<>());

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProduct(product);
            orderProduct.setQuantity(2);
            orderProduct.setOrder(order);
            orderProduct.setUnitPrice(product.getPrice());
            order.getItems().add(orderProduct);

            when(orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(1L, "customer@example.com"))
                    .thenReturn(List.of(orderProduct));
            when(productRepo.findByName("Chicken Wings")).thenReturn(Optional.of(product));
            when(productRepo.save(product)).thenReturn(product);
            when(orderProductRepo.save(orderProduct)).thenReturn(orderProduct);
            when(orderRepo.save(order)).thenReturn(order);

            OrderProductForCustomerOutputDto dto = orderProductService.updateOrderProductQuantity(
                    1L, "Chicken Wings", 5, "customer@example.com"
            );

            assertEquals(5, dto.quantity());
            verify(productRepo).save(product);
            verify(orderProductRepo).save(orderProduct);
            verify(orderRepo).save(order);
        }

        @Test
        void testUpdateOrderProductQuantity_orderNotFound() {
            when(orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(1L, "customer@example.com"))
                    .thenReturn(List.of());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    orderProductService.updateOrderProductQuantity(1L, "Chicken Wings", 5, "customer@example.com")
            );

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        void testUpdateOrderProductQuantity_productNotInOrder() {
            Product productInOrder = new Product();
            productInOrder.setName("Chicken Breast");

            Order order = new Order();
            order.setOrderStatus(OrderStatus.CONFIRMED);

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProduct(productInOrder);
            orderProduct.setOrder(order);

            when(orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(1L, "customer@example.com"))
                    .thenReturn(List.of(orderProduct));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
                orderProductService.updateOrderProductQuantity(1L, "NonExisting", 5, "customer@example.com");
            });

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        void testUpdateOrderProductQuantity_orderStatusNotConfirmed() {
            Product product = new Product();
            product.setName("Chicken Wings"); // <--- Viktig!

            Order order = new Order();
            order.setOrderStatus(OrderStatus.SHIPPED); // Status som blokkerer oppdatering

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProduct(product);
            orderProduct.setOrder(order);

            when(orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(1L, "customer@example.com"))
                    .thenReturn(List.of(orderProduct));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
                orderProductService.updateOrderProductQuantity(1L, "Chicken Wings", 5, "customer@example.com");
            });

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }
    }

    @Nested
    class AddProductToOrderTests {

        @Test
        void testAddProductToOrder_success() {
            Product product = new Product();
            product.setProductId(1L);
            product.setQuantity(10);
            product.setPrice(BigDecimal.valueOf(5));
            product.setName("Chicken Wings");

            Customer customer = new Customer();
            customer.setEmail("customer@example.com");

            Order order = new Order();
            order.setOrderId(1L);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setItems(new ArrayList<>());
            order.setCustomer(customer);

            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(productRepo.save(product)).thenReturn(product);
            when(orderProductRepo.save(any(OrderProduct.class))).thenAnswer(i -> i.getArgument(0));
            when(orderRepo.save(order)).thenReturn(order);

            OrderProductForCustomerOutputDto dto = orderProductService.addProductToOrder(
                    1L, 1L, 5, "customer@example.com"
            );

            assertEquals(5, dto.quantity());
            assertEquals("Chicken Wings", dto.name());
            verify(productRepo).save(product);
            verify(orderProductRepo).save(any(OrderProduct.class));
            verify(orderRepo).save(order);
        }

        @Test
        void testAddProductToOrder_orderNotFound() {
            when(orderRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderProductService.addProductToOrder(1L, 1L, 5, "customer@example.com"));

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        void testAddProductToOrder_notEnoughStock() {
            Product product = new Product();
            product.setProductId(1L);
            product.setQuantity(2);
            product.setPrice(BigDecimal.valueOf(5));
            product.setName("Chicken Wings");

            Customer customer = new Customer();
            customer.setEmail("customer@example.com");

            Order order = new Order();
            order.setOrderId(1L);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setItems(new ArrayList<>());
            order.setCustomer(customer);

            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
            when(productRepo.findById(1L)).thenReturn(Optional.of(product));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderProductService.addProductToOrder(1L, 1L, 5, "customer@example.com"));

            assertEquals(400, ex.getStatusCode().value());
        }
    }

    @Nested
    class FindAllOrderProductsTests {

        @Test
        void testFindAllOrderProducts() {
            Product product = new Product();
            product.setName("Chicken Wings");

            OrderProduct op = new OrderProduct();
            op.setProduct(product);
            op.setQuantity(2);
            op.setUnitPrice(BigDecimal.valueOf(5));

            when(orderProductRepo.findAll()).thenReturn(List.of(op));

            List<OrderProductForCustomerOutputDto> result = orderProductService.findAllOrderProducts();

            assertEquals(1, result.size());
            assertEquals("Chicken Wings", result.get(0).name());
        }
    }

    @Nested
    class FindOrderProductByIdTests {

        @Test
        void testFindOrderProductById_success() {
            Product product = new Product();
            product.setName("Chicken Wings");

            OrderProduct op = new OrderProduct();
            op.setProduct(product);
            op.setQuantity(2);
            op.setUnitPrice(BigDecimal.valueOf(5));

            when(orderProductRepo.findById(1L)).thenReturn(Optional.of(op));

            OrderProductForCustomerOutputDto dto = orderProductService.findOrderProductById(1L);

            assertEquals("Chicken Wings", dto.name());
            assertEquals(2, dto.quantity());
        }

        @Test
        void testFindOrderProductById_notFound() {
            when(orderProductRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderProductService.findOrderProductById(1L));

            assertEquals(404, ex.getStatusCode().value());
        }
    }

    @Nested
    class GetOrderProductsForCustomerTests {

        @Test
        void testGetOrderProductsForCustomer_success() {
            Product product = new Product();
            product.setName("Chicken Wings");

            OrderProduct op = new OrderProduct();
            op.setProduct(product);
            op.setQuantity(2);
            op.setUnitPrice(BigDecimal.valueOf(5));

            when(orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(1L, "customer@example.com"))
                    .thenReturn(List.of(op));

            List<OrderProductForCustomerOutputDto> result = orderProductService.getOrderProductsForCustomer(
                    1L, "customer@example.com"
            );

            assertEquals(1, result.size());
            assertEquals("Chicken Wings", result.get(0).name());
        }

        @Test
        void testGetOrderProductsForCustomer_forbidden() {
            when(orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(1L, "customer@example.com"))
                    .thenReturn(List.of());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderProductService.getOrderProductsForCustomer(1L, "customer@example.com"));

            assertEquals(403, ex.getStatusCode().value());
        }
    }

    @Nested
    class DeleteProductFromOrderTests {

        @Test
        void testDeleteProductFromOrder_success() {
            Product product = new Product();
            product.setProductId(1L);
            product.setQuantity(5);

            Customer customer = new Customer();
            customer.setEmail("customer@example.com");

            Order order = new Order();
            order.setOrderId(1L);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setItems(new ArrayList<>());
            order.setCustomer(customer);

            OrderProduct op = new OrderProduct();
            op.setProduct(product);
            op.setQuantity(2);

            order.getItems().add(op);

            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

            orderProductService.deleteProductFromOrder(1L, 1L, "customer@example.com");

            assertTrue(order.getItems().isEmpty());
            assertEquals(7, product.getQuantity());
            verify(productRepo).save(product);
            verify(orderProductRepo).delete(op);
            verify(orderRepo).save(order);
        }

        @Test
        void testDeleteProductFromOrder_orderNotFound() {
            when(orderRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderProductService.deleteProductFromOrder(1L, 1L, "customer@example.com"));

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        void testDeleteProductFromOrder_forbidden() {
            Product product = new Product();
            product.setProductId(1L);

            Customer customer = new Customer();
            customer.setEmail("someoneelse@example.com");

            Order order = new Order();
            order.setOrderId(1L);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setCustomer(customer);

            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderProductService.deleteProductFromOrder(1L, 1L, "customer@example.com"));

            assertEquals(403, ex.getStatusCode().value());
        }

        @Test
        void testDeleteProductFromOrder_orderStatusNotConfirmed() {
            Product product = new Product();
            product.setProductId(1L);

            Customer customer = new Customer();
            customer.setEmail("customer@example.com");

            Order order = new Order();
            order.setOrderId(1L);
            order.setOrderStatus(OrderStatus.SHIPPED);
            order.setCustomer(customer);

            OrderProduct op = new OrderProduct();
            op.setProduct(product);
            op.setQuantity(2);
            order.setItems(List.of(op));

            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> orderProductService.deleteProductFromOrder(1L, 1L, "customer@example.com"));

            assertEquals(400, ex.getStatusCode().value());
        }
    }
}
