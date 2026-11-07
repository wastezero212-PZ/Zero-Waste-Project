package com.example.zerowaste.controller;

import com.example.zerowaste.dto.cart.AddCartItemRequestDto;
import com.example.zerowaste.dto.cart.PlaceOrderResponseDto;
import com.example.zerowaste.dto.cart.ShoppingCartResponseDto;
import com.example.zerowaste.dto.cart.UpdateCartItemRequestDto;
import com.example.zerowaste.model.User;
import com.example.zerowaste.service.ShoppingCartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    public ShoppingCartController(
            ShoppingCartService cartService
    ) {
        this.cartService = cartService;
    }

    @GetMapping
    public ShoppingCartResponseDto getCart(
            @AuthenticationPrincipal User user
    ) {
        return cartService.getCart(user.getId());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCartResponseDto addItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddCartItemRequestDto request
    ) {
        return cartService.addItem(user.getId(), request);
    }

    @PutMapping("/items/{itemId}")
    public ShoppingCartResponseDto updateItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequestDto request
    ) {
        return cartService.updateItem(
                user.getId(),
                itemId,
                request
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ShoppingCartResponseDto removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId
    ) {
        return cartService.removeItem(
                user.getId(),
                itemId
        );
    }

    @PostMapping({"", "/"})
    public PlaceOrderResponseDto placeOrder(
            @AuthenticationPrincipal User user
    ) {
        return cartService.placeOrder(user.getId());
    }
}
