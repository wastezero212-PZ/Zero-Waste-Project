package com.example.zerowaste.service;

import com.example.zerowaste.dto.order.OrderResponseDto;
import com.example.zerowaste.model.Order;
import java.util.List;

public interface OrderService {

    Order createOrder(String buyerEmail);

    List<OrderResponseDto> getMyOrders(String buyerEmail);

    List<OrderResponseDto> getSellerOrders(
            Long sellerId, String requesterEmail);

    List<OrderResponseDto.OrderItemDto> getOrderItems(
            Long orderId, String requesterEmail);
}
