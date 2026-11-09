package com.example.zerowaste.repository;

import com.example.zerowaste.model.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyer_IdOrderByOrderDateDesc(Long buyerId);

    List<Order> findBySeller_IdOrderByOrderDateDesc(Long sellerId);
}
