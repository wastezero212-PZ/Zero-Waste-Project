package com.example.zerowaste.service.impl;

import com.example.zerowaste.dto.shoppingcart.CartItemRequestDto;
import com.example.zerowaste.dto.shoppingcart.ShoppingCartResponseDto;
import com.example.zerowaste.exception.BuyYourOwnItemException;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.mapper.CartMapper;
import com.example.zerowaste.model.CartItem;
import com.example.zerowaste.model.Product;
import com.example.zerowaste.model.ShoppingCart;
import com.example.zerowaste.repository.CartItemRepository;
import com.example.zerowaste.repository.ProductRepository;
import com.example.zerowaste.repository.ShoppingCartRepository;
import com.example.zerowaste.service.ShoppingCartService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Objects;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private ProductRepository productRepository;
    private ShoppingCartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private CartMapper cartMapper;

    @Override
    public ShoppingCartResponseDto getCartByOwnerId(Long userId) {
        ShoppingCart cart = cartRepository.findByOwner_Id(userId).orElseThrow(
                () -> new EntityNotFoundException("Cart not found for user with id: " + userId)
        );

        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartResponseDto addItem(Long userId, CartItemRequestDto request) {
        ShoppingCart cart = cartRepository.findByOwner_Id(userId).orElseThrow(
                () -> new EntityNotFoundException("Cart not found for user with id: " + userId)
        );

        Product product = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new EntityNotFoundException("Product not found with id: " + request.getProductId())
        );

        if (Objects.equals(product.getSeller().getId(), userId)) {
            throw new BuyYourOwnItemException("Cannot add your own product to cart.");
        }

        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(request.getQuantity()));

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        item.setShoppingCart(cart);
        item.setTotalPrice(totalPrice);

        cartItemRepository.save(item);
        cart.getCartItems().add(item);

        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartResponseDto updateItem(Long userId, Long itemId, CartItemRequestDto request) {
        ShoppingCart cart = cartRepository.findByOwner_Id(userId).orElseThrow(
                () -> new EntityNotFoundException("Cart not found for user with id: " + userId)
        );

        CartItem item = cartItemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException("Cart item not found with id: " + itemId)
        );

        if (!item.getShoppingCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item with id: " + itemId + " does not belong to user with id: " + userId);
        }

        Product product = item.getProduct();
        if (request.getProductId() != null && !request.getProductId().equals(product.getId())) {
            product = productRepository.findById(request.getProductId()).orElseThrow(
                    () -> new EntityNotFoundException("Product not found with id: " + request.getProductId())
            );
            item.setProduct(product);
        }

        item.setQuantity(request.getQuantity());
        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(request.getQuantity()));
        item.setTotalPrice(totalPrice);

        cartItemRepository.save(item);

        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long itemId) {
        ShoppingCart cart = cartRepository.findByOwner_Id(userId).orElseThrow(
                () -> new EntityNotFoundException("Cart not found for user with id: " + userId)
        );

        CartItem item = cartItemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException("Cart item not found with id: " + itemId)
        );

        if (!item.getShoppingCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item with id: " + itemId + " does not belong to user with id: " + userId);
        }

        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
    }
}
