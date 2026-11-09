package com.example.zerowaste.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.zerowaste.dto.order.OrderResponseDto;
import com.example.zerowaste.exception.CustomGlobalExceptionHandler;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.exception.OrderException;
import com.example.zerowaste.model.Order;
import com.example.zerowaste.model.OrderItem;
import com.example.zerowaste.model.Product;
import com.example.zerowaste.model.User;
import com.example.zerowaste.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderControllerTest {

    private MockMvc mockMvc;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new OrderController(orderService))
                .setControllerAdvice(new CustomGlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn201WhenOrderIsCreated() throws Exception {
        User seller = new User();
        seller.setId(2L);

        Product product = new Product();
        product.setId(5L);

        Order order = new Order();
        order.setId(1L);
        order.setSeller(seller);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(new BigDecimal("40.00"));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setTotalPrice(new BigDecimal("40.00"));
        order.addOrderItem(item);

        when(orderService.createOrder("buyer@example.com"))
                .thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .principal(authentication()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sellerId").value(2))
                .andExpect(jsonPath("$.items[0].productId").value(5))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void shouldReturn400WhenCartIsEmpty() throws Exception {
        when(orderService.createOrder("buyer@example.com"))
                .thenThrow(new OrderException("Shopping cart is empty"));

        mockMvc.perform(post("/api/orders")
                        .principal(authentication()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors")
                        .value("Shopping cart is empty"));
    }

    @Test
    void shouldReturn404WhenCartDoesNotExist() throws Exception {
        when(orderService.createOrder("buyer@example.com"))
                .thenThrow(new EntityNotFoundException(
                        "Shopping cart not found"));

        mockMvc.perform(post("/api/orders")
                        .principal(authentication()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors")
                        .value("Shopping cart not found"));
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(
                "buyer@example.com", null, List.of());
    }

    @Test
    void shouldReturn200WhenGettingMyOrders() throws Exception {
        when(orderService.getMyOrders("buyer@example.com"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/orders/my")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturn200WhenGettingSellerOrders() throws Exception {
        when(orderService.getSellerOrders(2L, "buyer@example.com"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/orders/seller/2")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturn200WhenGettingOrderItems() throws Exception {
        OrderResponseDto.OrderItemDto item =
                new OrderResponseDto.OrderItemDto(
                        5L,
                        2,
                        new BigDecimal("40.00")
                );

        when(orderService.getOrderItems(1L, "buyer@example.com"))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/orders/1/items")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(5))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].totalPrice").value(40.00));
    }
}
