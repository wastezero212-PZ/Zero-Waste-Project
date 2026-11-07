package com.example.zerowaste.service.impl;

import com.example.zerowaste.dto.cart.AddCartItemRequestDto;
import com.example.zerowaste.dto.cart.CartItemResponseDto;
import com.example.zerowaste.dto.cart.CreatedOrderDto;
import com.example.zerowaste.dto.cart.PlaceOrderResponseDto;
import com.example.zerowaste.dto.cart.ShoppingCartResponseDto;
import com.example.zerowaste.dto.cart.UpdateCartItemRequestDto;
import com.example.zerowaste.model.CartItem;
import com.example.zerowaste.model.Order;
import com.example.zerowaste.model.OrderItem;
import com.example.zerowaste.model.Product;
import com.example.zerowaste.model.ShoppingCart;
import com.example.zerowaste.model.User;
import com.example.zerowaste.repository.CartItemRepository;
import com.example.zerowaste.repository.OrderRepository;
import com.example.zerowaste.repository.ProductRepository;
import com.example.zerowaste.repository.ShoppingCartRepository;
import com.example.zerowaste.repository.UserRepository;
import com.example.zerowaste.service.ShoppingCartService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ShoppingCartServiceImpl(
            ShoppingCartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ShoppingCartResponseDto getCart(Long userId) {
        ShoppingCart cart = getOrCreateCart(userId);
        return mapToResponse(cart);
    }

    @Override
    public ShoppingCartResponseDto addItem(
            Long userId,
            AddCartItemRequestDto request
    ) {
        if (request.getQuantity() == null
                || request.getQuantity() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        ShoppingCart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(
                request.getProductId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found"
        ));

        CartItem existingItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId()
                        .equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(
                    existingItem.getQuantity() + request.getQuantity()
            );
            updateItemPrice(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setShoppingCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());

            updateItemPrice(newItem);
            cart.getCartItems().add(newItem);
        }

        updateCartTotalPrice(cart);

        ShoppingCart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    public ShoppingCartResponseDto updateItem(
            Long userId,
            Long itemId,
            UpdateCartItemRequestDto request
    ) {
        if (request.getQuantity() == null
                || request.getQuantity() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        ShoppingCart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByIdAndShoppingCartOwnerId(itemId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));

        item.setQuantity(request.getQuantity());
        updateItemPrice(item);
        updateCartTotalPrice(cart);

        return mapToResponse(cartRepository.save(cart));
    }

    @Override
    public ShoppingCartResponseDto removeItem(
            Long userId,
            Long itemId
    ) {
        ShoppingCart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByIdAndShoppingCartOwnerId(itemId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));

        cart.getCartItems().remove(item);
        updateCartTotalPrice(cart);

        return mapToResponse(cartRepository.save(cart));
    }

    @Override
    public PlaceOrderResponseDto placeOrder(Long userId) {
        ShoppingCart cart = cartRepository.findByOwnerId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shopping cart not found"
                ));

        if (cart.getCartItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Shopping cart is empty"
            );
        }

        User buyer = cart.getOwner();

        Map<Long, List<CartItem>> itemsBySeller =
                new LinkedHashMap<>();

        for (CartItem cartItem : cart.getCartItems()) {
            Long sellerId = cartItem.getProduct()
                    .getSeller()
                    .getId();

            itemsBySeller.computeIfAbsent(
                    sellerId,
                    key -> new ArrayList<>()
            ).add(cartItem);
        }

        List<CreatedOrderDto> createdOrders = new ArrayList<>();

        for (List<CartItem> sellerItems : itemsBySeller.values()) {
            User seller = sellerItems.get(0)
                    .getProduct()
                    .getSeller();

            Order order = new Order();
            order.setBuyer(buyer);
            order.setSeller(seller);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalPrice(BigDecimal.ZERO);

            BigDecimal orderTotal = BigDecimal.ZERO;

            for (CartItem cartItem : sellerItems) {
                OrderItem orderItem = new OrderItem();

                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setTotalPrice(cartItem.getTotalPrice());

                order.getOrderItems().add(orderItem);

                orderTotal = orderTotal.add(
                        cartItem.getTotalPrice()
                );
            }

            order.setTotalPrice(orderTotal);

            Order savedOrder = orderRepository.save(order);

            createdOrders.add(new CreatedOrderDto(
                    savedOrder.getId(),
                    seller.getId(),
                    savedOrder.getTotalPrice(),
                    savedOrder.getOrderItems().size()
            ));
        }

        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return new PlaceOrderResponseDto(
                buyer.getId(),
                createdOrders
        );
    }

    private ShoppingCart getOrCreateCart(Long userId) {
        return cartRepository.findByOwnerId(userId)
                .orElseGet(() -> {
                    User owner = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "User not found"
                            ));

                    ShoppingCart cart = new ShoppingCart();
                    cart.setOwner(owner);
                    cart.setTotalPrice(BigDecimal.ZERO);

                    return cartRepository.save(cart);
                });
    }

    private void updateItemPrice(CartItem item) {
        BigDecimal totalPrice = item.getProduct()
                .getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        item.setTotalPrice(totalPrice);
    }

    private void updateCartTotalPrice(ShoppingCart cart) {
        BigDecimal total = cart.getCartItems()
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(total);
    }

    private ShoppingCartResponseDto mapToResponse(
            ShoppingCart cart
    ) {
        List<CartItemResponseDto> items = cart.getCartItems()
                .stream()
                .map(item -> new CartItemResponseDto(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getTitle(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getTotalPrice(),
                        item.getProduct().getSeller().getId()
                ))
                .toList();

        return new ShoppingCartResponseDto(
                cart.getId(),
                cart.getOwner().getId(),
                items,
                cart.getTotalPrice()
        );
    }
}
