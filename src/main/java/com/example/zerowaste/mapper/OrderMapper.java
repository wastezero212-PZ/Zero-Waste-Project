package com.example.zerowaste.mapper;

import com.example.zerowaste.config.MapperConfig;
import com.example.zerowaste.dto.order.OrderDto;
import com.example.zerowaste.dto.order.OrderItemDto;
import com.example.zerowaste.model.CartItem;
import com.example.zerowaste.model.Order;
import com.example.zerowaste.model.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface OrderMapper {

    default OrderDto toDto(Order order) {

        List<OrderItemDto> list = order.getOrderItems().stream()
                .map(this::toOrderItemDto)
                .toList();

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setSellerId(order.getSeller().getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderDate(order.getOrderDate());
        dto.setItems(list);

        return dto;
    }

    default OrderItem toEntity(CartItem cartItem) {
        OrderItem item = new OrderItem();
        item.setProduct(cartItem.getProduct());
        item.setQuantity(cartItem.getQuantity());
        item.setTotalPrice(cartItem.getProduct().getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
        return item;
    }

    OrderItemDto toOrderItemDto(OrderItem orderItem);
}
