package com.example.zerowaste.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.zerowaste.dto.order.OrderResponseDto;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.model.CartItem;
import com.example.zerowaste.model.Order;
import com.example.zerowaste.model.OrderItem;
import com.example.zerowaste.model.Product;
import com.example.zerowaste.model.ShoppingCart;
import com.example.zerowaste.model.User;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:orderstest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.liquibase.enabled=false"
})

@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateOrderFromShoppingCart() {

        User buyer = new User();
        buyer.setEmail("buyer-order-test@example.com");
        buyer.setPassword("test-password");
        buyer.setFirstName("Test");
        buyer.setLastName("Buyer");
        entityManager.persist(buyer);

        User seller = new User();
        seller.setEmail("seller-order-test@example.com");
        seller.setPassword("test-password");
        seller.setFirstName("Test");
        seller.setLastName("Seller");
        entityManager.persist(seller);

        Product product = new Product();
        product.setTitle("Test Product");
        product.setPrice(new BigDecimal("20.00"));
        product.setSeller(seller);
        entityManager.persist(product);

        ShoppingCart cart = new ShoppingCart();
        cart.setOwner(buyer);
        cart.setTotalPrice(new BigDecimal("60.00"));
        entityManager.persist(cart);

        CartItem cartItem = new CartItem();
        cartItem.setShoppingCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(3);
        cartItem.setTotalPrice(new BigDecimal("60.00"));
        cart.getCartItems().add(cartItem);

        entityManager.persist(cartItem);
        entityManager.flush();
        entityManager.clear();

        Order order = orderService.createOrder(buyer.getEmail());

        entityManager.flush();

        assertNotNull(order.getId());
        assertNotNull(order.getOrderDate());

        assertEquals(buyer.getId(), order.getBuyer().getId());
        assertEquals(seller.getId(), order.getSeller().getId());

        assertEquals(
                0,
                new BigDecimal("60.00").compareTo(order.getTotalPrice())
        );

        assertEquals(1, order.getOrderItems().size());

        ShoppingCart updatedCart = entityManager.find(
                ShoppingCart.class, cart.getId()
        );

        assertTrue(updatedCart.getCartItems().isEmpty());
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(updatedCart.getTotalPrice())
        );

        entityManager.flush();
        entityManager.clear();

        Order savedOrder = entityManager.find(
                Order.class, order.getId()
        );

        assertNotNull(savedOrder);
        assertEquals(1, savedOrder.getOrderItems().size());

        var savedItem = savedOrder.getOrderItems()
                .iterator()
                .next();

        assertNotNull(savedItem.getId());
        assertEquals(product.getId(), savedItem.getProduct().getId());
        assertEquals(3, savedItem.getQuantity());
        assertEquals(
                0,
                new BigDecimal("60.00")
                        .compareTo(savedItem.getTotalPrice())
        );

        ShoppingCart savedCart = entityManager.find(
                ShoppingCart.class, cart.getId()
        );

        assertTrue(savedCart.getCartItems().isEmpty());
    }

    @Test
    void shouldRejectOrderWhenShoppingCartIsEmpty() {

        User buyer = new User();
        buyer.setEmail("empty-cart-test@example.com");
        buyer.setPassword("test-password");
        buyer.setFirstName("Test");
        buyer.setLastName("Buyer");
        entityManager.persist(buyer);

        ShoppingCart cart = new ShoppingCart();
        cart.setOwner(buyer);
        cart.setTotalPrice(BigDecimal.ZERO);
        entityManager.persist(cart);

        entityManager.flush();
        entityManager.clear();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(buyer.getEmail())
        );

        assertEquals(
                "Shopping cart is empty",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectOrderWithDifferentSellers() {

        User buyer = new User();
        buyer.setEmail("buyer-multi@example.com");
        buyer.setPassword("test-password");
        buyer.setFirstName("Test");
        buyer.setLastName("Buyer");
        entityManager.persist(buyer);

        User seller1 = new User();
        seller1.setEmail("seller1@example.com");
        seller1.setPassword("test-password");
        seller1.setFirstName("First");
        seller1.setLastName("Seller");
        entityManager.persist(seller1);

        User seller2 = new User();
        seller2.setEmail("seller2@example.com");
        seller2.setPassword("test-password");
        seller2.setFirstName("Second");
        seller2.setLastName("Seller");
        entityManager.persist(seller2);

        Product product1 = new Product();
        product1.setTitle("Product A");
        product1.setPrice(new BigDecimal("20.00"));
        product1.setSeller(seller1);
        entityManager.persist(product1);

        Product product2 = new Product();
        product2.setTitle("Product B");
        product2.setPrice(new BigDecimal("30.00"));
        product2.setSeller(seller2);
        entityManager.persist(product2);

        ShoppingCart cart = new ShoppingCart();
        cart.setOwner(buyer);
        cart.setTotalPrice(new BigDecimal("50.00"));
        entityManager.persist(cart);

        CartItem item1 = new CartItem();
        item1.setShoppingCart(cart);
        item1.setProduct(product1);
        item1.setQuantity(1);
        item1.setTotalPrice(new BigDecimal("20.00"));
        cart.getCartItems().add(item1);
        entityManager.persist(item1);

        CartItem item2 = new CartItem();
        item2.setShoppingCart(cart);
        item2.setProduct(product2);
        item2.setQuantity(1);
        item2.setTotalPrice(new BigDecimal("30.00"));
        cart.getCartItems().add(item2);
        entityManager.persist(item2);

        entityManager.flush();
        entityManager.clear();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(buyer.getEmail())
        );

        assertEquals(
                "Products belong to different sellers",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectOrderWithInvalidQuantity() {

        User buyer = new User();
        buyer.setEmail("buyer-invalid-quantity@example.com");
        buyer.setPassword("test-password");
        buyer.setFirstName("Test");
        buyer.setLastName("Buyer");
        entityManager.persist(buyer);

        User seller = new User();
        seller.setEmail("seller-invalid-quantity@example.com");
        seller.setPassword("test-password");
        seller.setFirstName("Test");
        seller.setLastName("Seller");
        entityManager.persist(seller);

        Product product = new Product();
        product.setTitle("Test Product");
        product.setPrice(new BigDecimal("20.00"));
        product.setSeller(seller);
        entityManager.persist(product);

        ShoppingCart cart = new ShoppingCart();
        cart.setOwner(buyer);
        cart.setTotalPrice(BigDecimal.ZERO);
        entityManager.persist(cart);

        CartItem cartItem = new CartItem();
        cartItem.setShoppingCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(0);
        cartItem.setTotalPrice(BigDecimal.ZERO);
        cart.getCartItems().add(cartItem);
        entityManager.persist(cartItem);

        entityManager.flush();
        entityManager.clear();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(buyer.getEmail())
        );

        assertEquals("Invalid quantity", exception.getMessage());
    }

    @Test
    void shouldRejectOrderWhenBuyerDoesNotExist() {
        String buyerEmail = "nonexistent-buyer@example.com";

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.createOrder(buyerEmail)
        );

        assertEquals("Buyer not found", exception.getMessage());
    }

    @Test
    void shouldRejectOrderWithNegativeProductPrice() {

        User buyer = new User();
        buyer.setEmail("buyer-negative-price@example.com");
        buyer.setPassword("test-password");
        buyer.setFirstName("Test");
        buyer.setLastName("Buyer");
        entityManager.persist(buyer);

        User seller = new User();
        seller.setEmail("seller-negative-price@example.com");
        seller.setPassword("test-password");
        seller.setFirstName("Test");
        seller.setLastName("Seller");
        entityManager.persist(seller);

        Product product = new Product();
        product.setTitle("Invalid Price Product");
        product.setPrice(new BigDecimal("-10.00"));
        product.setSeller(seller);
        entityManager.persist(product);

        ShoppingCart cart = new ShoppingCart();
        cart.setOwner(buyer);
        cart.setTotalPrice(BigDecimal.ZERO);
        entityManager.persist(cart);

        CartItem cartItem = new CartItem();
        cartItem.setShoppingCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setTotalPrice(new BigDecimal("-10.00"));
        cart.getCartItems().add(cartItem);
        entityManager.persist(cartItem);

        entityManager.flush();
        entityManager.clear();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(buyer.getEmail())
        );

        assertEquals("Invalid product price", exception.getMessage());
    }

    @Test
    void shouldReturnOnlyOrdersOfLoggedInBuyer() {
        User buyer = persistOrderTestUser("buyer-history@example.com");
        User otherBuyer = persistOrderTestUser("other-buyer@example.com");
        User seller = persistOrderTestUser("seller-history@example.com");

        final Order olderOrder = persistOrderForTest(
                buyer, seller, LocalDateTime.of(2026, 11, 1, 12, 0));

        final Order newerOrder = persistOrderForTest(
                buyer, seller, LocalDateTime.of(2026, 11, 5, 12, 0));

        persistOrderForTest(
                otherBuyer, seller, LocalDateTime.of(2026, 11, 7, 12, 0));

        entityManager.flush();
        entityManager.clear();

        List<OrderResponseDto> orders = orderService.getMyOrders(buyer.getEmail());

        assertEquals(2, orders.size());
        assertEquals(newerOrder.getId(), orders.get(0).id());
        assertEquals(olderOrder.getId(), orders.get(1).id());
        assertEquals(1, orders.get(0).items().size());
    }

    private User persistOrderTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("test-password");
        user.setFirstName("Test");
        user.setLastName("User");

        entityManager.persist(user);
        return user;
    }

    private Order persistOrderForTest(
            User buyer, User seller, LocalDateTime orderDate) {

        Product product = new Product();
        product.setTitle("Test Product");
        product.setPrice(new BigDecimal("20.00"));
        product.setSeller(seller);
        entityManager.persist(product);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setOrderDate(orderDate);
        order.setTotalPrice(new BigDecimal("20.00"));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setTotalPrice(new BigDecimal("20.00"));

        order.addOrderItem(item);
        entityManager.persist(order);

        return order;
    }

    @Test
    void shouldReturnOnlyOrdersOfRequestedSeller() {
        User buyer = persistOrderTestUser("buyer-sales@example.com");
        User seller = persistOrderTestUser("seller-sales@example.com");
        User otherSeller = persistOrderTestUser("other-seller-sales@example.com");

        final Order olderOrder = persistOrderForTest(
                buyer, seller, LocalDateTime.of(2026, 11, 1, 12, 0));

        final Order newerOrder = persistOrderForTest(
                buyer, seller, LocalDateTime.of(2026, 11, 5, 12, 0));

        persistOrderForTest(
                buyer, otherSeller, LocalDateTime.of(2026, 11, 7, 12, 0));

        entityManager.flush();
        entityManager.clear();

        List<OrderResponseDto> orders = orderService.getSellerOrders(
                seller.getId(), seller.getEmail());

        assertEquals(2, orders.size());
        assertEquals(newerOrder.getId(), orders.get(0).id());
        assertEquals(olderOrder.getId(), orders.get(1).id());
        assertEquals(1, orders.get(0).items().size());
    }

    @Test
    void shouldRejectAccessToAnotherSellerOrders() {
        User seller = persistOrderTestUser("seller-access@example.com");
        User otherUser = persistOrderTestUser("other-user-access@example.com");

        entityManager.flush();
        entityManager.clear();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getSellerOrders(
                        seller.getId(), otherUser.getEmail())
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void shouldReturnOrderItemsToBuyerAndSeller() {
        User buyer = persistOrderTestUser("buyer-items@example.com");
        User seller = persistOrderTestUser("seller-items@example.com");

        Order order = persistOrderForTest(
                buyer, seller, LocalDateTime.of(2026, 11, 5, 12, 0));

        entityManager.flush();
        entityManager.clear();

        List<OrderResponseDto.OrderItemDto> buyerItems =
                orderService.getOrderItems(order.getId(), buyer.getEmail());

        assertEquals(1, buyerItems.size());
        assertEquals(1, buyerItems.get(0).quantity());
        assertEquals(
                0,
                new BigDecimal("20.00")
                        .compareTo(buyerItems.get(0).totalPrice())
        );

        List<OrderResponseDto.OrderItemDto> sellerItems =
                orderService.getOrderItems(order.getId(), seller.getEmail());

        assertEquals(1, sellerItems.size());
        assertEquals(
                buyerItems.get(0).productId(),
                sellerItems.get(0).productId()
        );
    }

    @Test
    void shouldRejectOrderItemsAccessForUnrelatedUser() {
        User buyer = persistOrderTestUser("buyer-private@example.com");
        User seller = persistOrderTestUser("seller-private@example.com");
        User unrelatedUser = persistOrderTestUser("unrelated@example.com");

        Order order = persistOrderForTest(
                buyer, seller, LocalDateTime.of(2026, 11, 5, 12, 0));

        entityManager.flush();
        entityManager.clear();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderItems(
                        order.getId(), unrelatedUser.getEmail())
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void shouldRejectOrderItemsWhenOrderDoesNotExist() {
        User requester = persistOrderTestUser("requester-missing@example.com");

        entityManager.flush();
        entityManager.clear();

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.getOrderItems(
                        99999999L, requester.getEmail())
        );

        assertEquals("Order not found", exception.getMessage());
    }
}
