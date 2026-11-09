package com.example.zerowaste.controller;

import com.example.zerowaste.dto.order.OrderResponseDto;
import com.example.zerowaste.model.Order;
import com.example.zerowaste.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            Authentication authentication) {

        Order order = orderService.createOrder(authentication.getName());

        OrderResponseDto response = OrderResponseDto.from(order);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(
            Authentication authentication) {
        List<OrderResponseDto> orders = orderService.getMyOrders(
                authentication.getName());

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/seller/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getSellerOrders(
            @PathVariable Long userId,
            Authentication authentication) {
        List<OrderResponseDto> orders = orderService.getSellerOrders(
                userId, authentication.getName());

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderResponseDto.OrderItemDto>> getOrderItems(
            @PathVariable Long orderId,
            Authentication authentication) {
        List<OrderResponseDto.OrderItemDto> items = orderService.getOrderItems(
                orderId, authentication.getName());

        return ResponseEntity.ok(items);
    }
}
