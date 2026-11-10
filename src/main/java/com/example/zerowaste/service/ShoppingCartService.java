package com.example.zerowaste.service;

import com.example.zerowaste.dto.shoppingcart.CartItemRequestDto;
import com.example.zerowaste.dto.shoppingcart.ShoppingCartResponseDto;

public interface ShoppingCartService {

    ShoppingCartResponseDto getCartByOwnerId(Long userId);

    ShoppingCartResponseDto addItem(Long userId, CartItemRequestDto request);

    ShoppingCartResponseDto updateItem(Long userId, Long itemId, CartItemRequestDto request);

    void removeItem(Long userId, Long itemId);
}
