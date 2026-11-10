package com.example.zerowaste.controller;

import com.example.zerowaste.dto.order.OrderDto;
import com.example.zerowaste.dto.order.OrderItemDto;
import com.example.zerowaste.model.User;
import com.example.zerowaste.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order Controller", description = "Endpoints for managing orders")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new orders from authenticated users shopping cart")
    public List<OrderDto> createOrder(@AuthenticationPrincipal User user) {
        return orderService.createOrders(user.getId());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all orders ordered by the authenticated user paginated")
    public Page<OrderDto> getMyOrders(@AuthenticationPrincipal User user, Pageable pageable) {
        return orderService.getOrdersByUserId(user.getId(), pageable);
    }

    @GetMapping("/seller/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all orders of the seller paginated")
    public Page<OrderDto> getSellerOrders(@PathVariable Long userId, Pageable pageable) {
        return orderService.getSellerOrders(userId, pageable);
    }

    @GetMapping("/{orderId}/items")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all items from order with id = order id paginated")
    public Page<OrderItemDto> getOrderItems(@PathVariable Long orderId, Pageable pageable) {
        return orderService.getOrderItems(orderId, pageable);
    }

    // Admin endpoints

    @GetMapping("/buyer/{buyerId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all orders ordered by user with id = buyer id paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderDto> getOrdersByBuyerId(@PathVariable Long buyerId, Pageable pageable) {
        return orderService.getOrdersByUserId(buyerId, pageable);
    }
}
