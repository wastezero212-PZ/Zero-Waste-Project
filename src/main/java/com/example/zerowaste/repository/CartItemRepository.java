package com.example.zerowaste.repository;

import com.example.zerowaste.model.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndShoppingCartOwnerId(
            Long itemId,
            Long ownerId
    );
}
