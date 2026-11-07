package com.example.zerowaste.dto.cart;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShoppingCartResponseDto {

    private Long id;
    private Long ownerId;
    private List<CartItemResponseDto> cartItems;
    private BigDecimal totalPrice;
}
