package org.example.chickendirect.repos;

import org.example.chickendirect.entities.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductRepo extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findByOrderOrderIdAndOrderCustomerEmail(long orderId, String email);
}
