package com.example.zerowaste.service.impl;

import com.example.zerowaste.dto.cart.AddCartItemRequestDto;
import com.example.zerowaste.dto.cart.CreatedOrderDto;
import com.example.zerowaste.dto.cart.PlaceOrderResponseDto;
import com.example.zerowaste.dto.cart.ShoppingCartResponseDto;
import com.example.zerowaste.dto.cart.UpdateCartItemRequestDto;
import com.example.zerowaste.exception.EmptyShoppingCartException;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.mapper.CartItemMapper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final CartItemMapper cartItemMapper;

    public ShoppingCartServiceImpl(
            ShoppingCartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            CartItemMapper cartItemMapper
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartItemMapper = cartItemMapper;
    }

    @Override
    public ShoppingCartResponseDto getCart(Long userId) {
        ShoppingCart cart = getOrCreateCart(userId);
        return cartItemMapper.toDto(cart);
    }

    @Override
    public ShoppingCartResponseDto addItem(
            Long userId,
            AddCartItemRequestDto request
    ) {
        validateQuantity(request.getQuantity());

        ShoppingCart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found with id: " + request.getProductId()
                ));

        CartItem existingItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
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
        return cartItemMapper.toDto(savedCart);
    }

    @Override
    public ShoppingCartResponseDto updateItem(
            Long userId,
            Long itemId,
            UpdateCartItemRequestDto request
    ) {
        validateQuantity(request.getQuantity());

        ShoppingCart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByIdAndShoppingCartOwnerId(itemId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cart item not found with id: " + itemId
                ));

        item.setQuantity(request.getQuantity());
        updateItemPrice(item);
        updateCartTotalPrice(cart);

        return cartItemMapper.toDto(cartRepository.save(cart));
    }

    @Override
    public void removeItem(Long userId, Long itemId) {
        ShoppingCart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByIdAndShoppingCartOwnerId(itemId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cart item not found with id: " + itemId
                ));

        cart.getCartItems().remove(item);
        updateCartTotalPrice(cart);

        cartRepository.save(cart);
    }

    @Override
    public PlaceOrderResponseDto placeOrder(Long userId) {
        ShoppingCart cart = getOrCreateCart(userId);

        if (cart.getCartItems().isEmpty()) {
            throw new EmptyShoppingCartException(
                    "Shopping cart is empty"
            );
        }

        User buyer = cart.getOwner();

        Map<Long, List<CartItem>> itemsBySeller = cart.getCartItems()
                .stream()
                .collect(Collectors.groupingBy(
                        cartItem -> cartItem.getProduct().getSeller().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        final List<CreatedOrderDto> createdOrders = itemsBySeller.values()
                .stream()
                .map(sellerItems -> createOrder(buyer, sellerItems))
                .toList();

        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return new PlaceOrderResponseDto(
                buyer.getId(),
                createdOrders
        );
    }

    private CreatedOrderDto createOrder(
            User buyer,
            List<CartItem> sellerItems
    ) {
        User seller = sellerItems.get(0).getProduct().getSeller();

        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> orderItems = sellerItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = cartItemMapper.mapToOrderItem(cartItem);
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .toList();

        orderItems.forEach(order.getOrderItems()::add);

        BigDecimal orderTotal = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(orderTotal);

        Order savedOrder = orderRepository.save(order);

        return new CreatedOrderDto(
                savedOrder.getId(),
                seller.getId(),
                savedOrder.getTotalPrice(),
                savedOrder.getOrderItems().size()
        );
    }

    private ShoppingCart getOrCreateCart(Long userId) {
        return cartRepository.findByOwnerId(userId)
                .orElseGet(() -> {
                    User owner = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "User not found with id: " + userId
                            ));

                    ShoppingCart cart = new ShoppingCart();
                    cart.setOwner(owner);
                    cart.setTotalPrice(BigDecimal.ZERO);

                    return cartRepository.save(cart);
                });
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }
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
}
