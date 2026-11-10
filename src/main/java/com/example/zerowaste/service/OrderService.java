package com.example.zerowaste.service;

import com.example.zerowaste.dto.order.OrderDto;
import com.example.zerowaste.dto.order.OrderItemDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    List<OrderDto> createOrders(Long userId);

    Page<OrderDto> getOrdersByUserId(Long userId, Pageable pageable);

    Page<OrderDto> getSellerOrders(Long userId, Pageable pageable);

    Page<OrderItemDto> getOrderItems(Long orderId, Pageable pageable);
}
