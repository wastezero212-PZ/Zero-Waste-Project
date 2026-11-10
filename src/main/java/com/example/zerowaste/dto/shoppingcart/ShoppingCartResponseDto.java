package com.example.zerowaste.dto.shoppingcart;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShoppingCartResponseDto {

    private Long id;
    private Long ownerId;
    private List<CartItemResponseDto> cartItems;
    private BigDecimal totalPrice;
}
