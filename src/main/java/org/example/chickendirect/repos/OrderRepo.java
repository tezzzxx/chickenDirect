package org.example.chickendirect.repos;

import org.example.chickendirect.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderIdAndCustomerEmail(long orderId, String email);

    List<Order> findByCustomerCustomerId(Long customerId);

    boolean existsByAddress_AddressId(Long addressId);

    boolean existsByCustomer_CustomerId(Long customerId);
}