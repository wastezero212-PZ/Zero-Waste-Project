package com.example.zerowaste.controller;

import com.example.zerowaste.dto.order.OrderResponseDto;
import com.example.zerowaste.model.Order;
import com.example.zerowaste.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
}
