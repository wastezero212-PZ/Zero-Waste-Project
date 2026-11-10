package com.example.zerowaste.service;

import com.example.zerowaste.dto.cart.AddCartItemRequestDto;
import com.example.zerowaste.dto.cart.PlaceOrderResponseDto;
import com.example.zerowaste.dto.cart.ShoppingCartResponseDto;
import com.example.zerowaste.dto.cart.UpdateCartItemRequestDto;

public interface ShoppingCartService {

    ShoppingCartResponseDto getCart(Long userId);

    ShoppingCartResponseDto addItem(
            Long userId,
            AddCartItemRequestDto request
    );

    ShoppingCartResponseDto updateItem(
            Long userId,
            Long itemId,
            UpdateCartItemRequestDto request
    );

    void removeItem(
            Long userId,
            Long itemId
    );

    PlaceOrderResponseDto placeOrder(Long userId);
}
