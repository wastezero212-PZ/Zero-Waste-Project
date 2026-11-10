package com.example.zerowaste.mapper;

import com.example.zerowaste.config.MapperConfig;
import com.example.zerowaste.dto.shoppingcart.ShoppingCartResponseDto;
import com.example.zerowaste.model.ShoppingCart;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CartMapper {
    ShoppingCartResponseDto toDto(ShoppingCart cart);
}
