package org.example.chickendirect.integration.repo;

import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.entities.*;
import org.example.chickendirect.enums.OrderStatus;
import org.example.chickendirect.repos.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
public class OrderProductRepoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderProductRepo orderProductRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private ProductRepo productRepo;

    @Test
    void findByOrderOrderIdAndOrderCustomerEmail_shouldReturnOrderProducts() {
        Customer customer = new Customer();
        customer.setName("Cardi B");
        customer.setPhoneNumber("12345678");
        customer.setEmail("cardi@b.no");
        customerRepo.save(customer);

        Address address = new Address();
        address.setApartmentNumber("1A");
        address.setAddress("Famous road");
        address.setZipCode("0123");
        address.setCity("LA");
        address.setCountry("USA");
        addressRepo.save(address);

        Order order = new Order();
        order.setCustomer(customer);
        order.setAddress(address);
        order.setDate(LocalDate.now());
        order.setTotalSum(BigDecimal.valueOf(500));
        order.setShippingCharge(BigDecimal.valueOf(50));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order);

        Product product = new Product();
        product.setName("Chicken Breast");
        product.setPrice(BigDecimal.valueOf(100));
        productRepo.save(product);

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrder(order);
        orderProduct.setProduct(product);
        orderProduct.setQuantity(2);
        orderProduct.setUnitPrice(BigDecimal.valueOf(200));
        orderProductRepo.save(orderProduct);

        List<OrderProduct> result =
                orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(
                        order.getOrderId(),
                        "cardi@b.no"
                );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getOrder().getOrderId())
                .isEqualTo(order.getOrderId());
        assertThat(result.getFirst().getOrder().getCustomer().getEmail())
                .isEqualTo("cardi@b.no");
        assertThat(result.getFirst().getProduct().getName())
                .isEqualTo("Chicken Breast");
    }

    @Test
    void findByOrderOrderIdAndOrderCustomerEmail_shouldReturnEmpty_whenEmailDoesNotMatch() {
        Customer customer = new Customer();
        customer.setName("Lil Wayne");
        customer.setPhoneNumber("87654321");
        customer.setEmail("lil@wayne.no");
        customerRepo.save(customer);

        Address address = new Address();
        address.setApartmentNumber("2B");
        address.setAddress("Famous road");
        address.setZipCode("0456");
        address.setCity("LA");
        address.setCountry("USA");
        addressRepo.save(address);

        Order order = new Order();
        order.setCustomer(customer);
        order.setAddress(address);
        order.setDate(LocalDate.now());
        order.setTotalSum(BigDecimal.valueOf(300));
        order.setShippingCharge(BigDecimal.valueOf(30));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order);

        Product product = new Product();
        product.setName("Chicken Wings");
        product.setPrice(BigDecimal.valueOf(50));
        productRepo.save(product);

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrder(order);
        orderProduct.setProduct(product);
        orderProduct.setQuantity(1);
        orderProduct.setUnitPrice(BigDecimal.valueOf(50));
        orderProductRepo.save(orderProduct);

        List<OrderProduct> result =
                orderProductRepo.findByOrderOrderIdAndOrderCustomerEmail(
                        order.getOrderId(),
                        "not@right.no"
                );

        assertThat(result).isEmpty();
    }
}
