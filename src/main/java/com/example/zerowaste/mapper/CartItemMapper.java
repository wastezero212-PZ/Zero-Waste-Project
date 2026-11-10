package com.example.zerowaste.mapper;

import com.example.zerowaste.config.MapperConfig;
import com.example.zerowaste.dto.cart.CartItemResponseDto;
import com.example.zerowaste.dto.cart.ShoppingCartResponseDto;
import com.example.zerowaste.model.CartItem;
import com.example.zerowaste.model.OrderItem;
import com.example.zerowaste.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CartItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productTitle", source = "product.title")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "sellerId", source = "product.seller.id")
    CartItemResponseDto toDto(CartItem cartItem);

    @Mapping(target = "ownerId", source = "owner.id")
    ShoppingCartResponseDto toDto(ShoppingCart shoppingCart);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem mapToOrderItem(CartItem cartItem);
}
