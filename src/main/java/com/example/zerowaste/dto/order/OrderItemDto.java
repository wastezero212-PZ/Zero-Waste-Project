package com.example.zerowaste.dto.order;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {
    private Long id;
    private Long productId;
    private Long quantity;
    private BigDecimal priceTotal;
    private Long sellerId;
}
