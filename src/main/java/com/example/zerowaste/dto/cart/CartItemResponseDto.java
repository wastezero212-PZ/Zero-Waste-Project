package com.example.zerowaste.dto.cart;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemResponseDto {

    private Long id;
    private Long productId;
    private String productTitle;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Long sellerId;
}
