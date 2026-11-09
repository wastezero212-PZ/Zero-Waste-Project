package com.example.zerowaste.service.impl;

import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.exception.OrderException;
import com.example.zerowaste.model.CartItem;
import com.example.zerowaste.model.Order;
import com.example.zerowaste.model.OrderItem;
import com.example.zerowaste.model.Product;
import com.example.zerowaste.model.ShoppingCart;
import com.example.zerowaste.model.User;
import com.example.zerowaste.repository.OrderRepository;
import com.example.zerowaste.repository.UserRepository;
import com.example.zerowaste.service.OrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Order createOrder(String buyerEmail) {

        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() ->
                        new EntityNotFoundException("Buyer not found"));

        ShoppingCart cart = entityManager.createQuery(
                        "SELECT c FROM ShoppingCart c "
                                + "WHERE c.owner.id = :buyerId",
                        ShoppingCart.class)
                .setParameter("buyerId", buyer.getId())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Shopping cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new OrderException("Shopping cart is empty");
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setOrderDate(LocalDateTime.now());

        BigDecimal totalPrice = BigDecimal.ZERO;
        User seller = null;

        for (CartItem cartItem : cart.getCartItems()) {

            Product product = cartItem.getProduct();

            if (product == null || product.getSeller() == null) {
                throw new OrderException("Invalid product");
            }

            if (cartItem.getQuantity() <= 0) {
                throw new OrderException("Invalid quantity");
            }

            if (product.getPrice() == null
                    || product.getPrice().signum() < 0) {
                throw new OrderException("Invalid product price");
            }

            if (seller == null) {
                seller = product.getSeller();
            } else if (!Objects.equals(
                    seller.getId(), product.getSeller().getId())) {
                throw new OrderException(
                        "Products belong to different sellers");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            orderItem.setTotalPrice(itemTotal);
            order.addOrderItem(orderItem);

            totalPrice = totalPrice.add(itemTotal);
        }

        order.setSeller(seller);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);

        return savedOrder;
    }
}
