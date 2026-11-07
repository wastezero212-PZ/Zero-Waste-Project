package com.example.zerowaste.dto.cart;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatedOrderDto {

    private Long orderId;
    private Long sellerId;
    private BigDecimal totalPrice;
    private Integer itemCount;
}
