package com.example.zerowaste.service.impl;

import com.example.zerowaste.dto.order.OrderDto;
import com.example.zerowaste.dto.order.OrderItemDto;
import com.example.zerowaste.exception.EmptyShoppingCartException;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.exception.NotEnoughQuantityException;
import com.example.zerowaste.mapper.OrderMapper;
import com.example.zerowaste.model.CartItem;
import com.example.zerowaste.model.Order;
import com.example.zerowaste.model.OrderItem;
import com.example.zerowaste.model.Product;
import com.example.zerowaste.model.ShoppingCart;
import com.example.zerowaste.model.User;
import com.example.zerowaste.repository.CartItemRepository;
import com.example.zerowaste.repository.OrderItemRepository;
import com.example.zerowaste.repository.OrderRepository;
import com.example.zerowaste.repository.ProductRepository;
import com.example.zerowaste.repository.ShoppingCartRepository;
import com.example.zerowaste.repository.UserRepository;
import com.example.zerowaste.service.OrderService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public List<OrderDto> createOrders(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with id: " + userId)
        );

        ShoppingCart cart = cartRepository.findByOwner_Id(user.getId()).orElseThrow(
                () -> new EntityNotFoundException("Cart not found for user with id: " + user.getId())
        );

        if (cart.getCartItems().isEmpty()) {
            throw new EmptyShoppingCartException("Cannot create an order from an empty cart");
        }

        for (CartItem cartItem : cart.getCartItems()) {
            if (cartItem.getProduct().getQuantity() < cartItem.getQuantity()) {
                throw new NotEnoughQuantityException("Not enough quantity of product in stock.");
            }
        }

        Map<Long, List<CartItem>> itemsBySellerId = cart.getCartItems().stream()
                .collect(Collectors.groupingBy(cartItem -> cartItem.getProduct().getSeller().getId()));

        Map<Long, User> sellersMap = userRepository.findAllById(itemsBySellerId.keySet()).stream()
                .collect(Collectors.toMap(User::getId, seller -> seller));

        List<Order> createdOrders = new ArrayList<>();

        for (Map.Entry<Long, List<CartItem>> entry : itemsBySellerId.entrySet()) {
            Long sellerId = entry.getKey();
            List<CartItem> sellerCartItems = entry.getValue();

            User seller = sellersMap.get(sellerId);
            if (seller == null) {
                throw new EntityNotFoundException("Seller not found with id: " + sellerId);
            }

            Set<OrderItem> orderItems = sellerCartItems.stream()
                    .map(cartItem -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setProduct(cartItem.getProduct());
                        orderItem.setQuantity(cartItem.getQuantity());
                        orderItem.setTotalPrice(cartItem.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                        return orderItem;
                    })
                    .collect(Collectors.toSet());

            BigDecimal totalPrice = orderItems.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Order order = new Order();
            order.setOrderDate(LocalDateTime.now());
            order.setBuyer(user);
            order.setSeller(seller);
            order.setDeleted(false);
            order.setTotalPrice(totalPrice);

            orderItems.forEach(item -> item.setOrder(order));
            order.setOrderItems(orderItems);

            createdOrders.add(orderRepository.save(order));
        }

        for (CartItem cartItem : cart.getCartItems()) {

            Product product = productRepository.findById(cartItem.getProduct().getId()).orElseThrow(
                    () -> new EntityNotFoundException("Product not found with id: " + cartItem.getProduct().getId())
            );

            int newQuantity = product.getQuantity() - cartItem.getQuantity();
            product.setQuantity(newQuantity);
            productRepository.save(product);
        }

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

        return createdOrders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    public Page<OrderDto> getOrdersByUserId(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with id: " + userId)
        );

        return orderRepository.findAllByBuyer(user, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public Page<OrderDto> getSellerOrders(Long userId, Pageable pageable) {
        User seller = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with id: " + userId)
        );

        return orderRepository.findAllBySeller(seller, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public Page<OrderItemDto> getOrderItems(Long orderId, Pageable pageable) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Order not found with id: " + orderId)
        );

        return orderItemRepository.findAllByOrder(order, pageable)
                .map(orderMapper::toOrderItemDto);
    }
}
