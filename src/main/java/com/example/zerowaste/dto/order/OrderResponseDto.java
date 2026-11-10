package com.example.zerowaste.dto.order;

import com.example.zerowaste.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        LocalDateTime orderDate,
        BigDecimal totalPrice,
        Long sellerId,
        List<OrderItemDto> items
) {

    public record OrderItemDto(
            Long productId,
            int quantity,
            BigDecimal totalPrice
    ) {
    }

    public static OrderResponseDto from(Order order) {
        List<OrderItemDto> items = order.getOrderItems()
                .stream()
                .map(item -> new OrderItemDto(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        item.getTotalPrice()
                ))
                .toList();

        return new OrderResponseDto(
                order.getId(),
                order.getOrderDate(),
                order.getTotalPrice(),
                order.getSeller().getId(),
                items
        );
    }
}
