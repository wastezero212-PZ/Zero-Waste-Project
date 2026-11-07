package com.example.zerowaste.dto.cart;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaceOrderResponseDto {

    private Long buyerId;
    private List<CreatedOrderDto> orders;
}
