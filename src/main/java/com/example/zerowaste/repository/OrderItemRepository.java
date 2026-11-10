package com.example.zerowaste.repository;

import com.example.zerowaste.model.Order;
import com.example.zerowaste.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Page<OrderItem> findAllByOrder(Order order, Pageable pageable);
}
