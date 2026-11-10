package com.example.zerowaste.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDto {
    private Long id;
    private LocalDateTime orderDate;
    private BigDecimal totalPrice;
    private Long sellerId;
    private List<OrderItemDto> items;
}
