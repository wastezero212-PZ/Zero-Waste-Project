package com.example.zerowaste.repository;

import com.example.zerowaste.model.Order;
import com.example.zerowaste.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByBuyer(User buyer, Pageable pageable);

    Page<Order> findAllBySeller(User seller, Pageable pageable);
}
