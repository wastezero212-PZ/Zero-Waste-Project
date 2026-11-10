package com.example.zerowaste.mapper;

import com.example.zerowaste.config.MapperConfig;
import com.example.zerowaste.dto.product.ProductDto;
import com.example.zerowaste.dto.product.ProductRequestDto;
import com.example.zerowaste.model.Product;
import com.example.zerowaste.model.ProductCategory;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface ProductMapper {

    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "categories", ignore = true)
    Product toEntity(ProductRequestDto requestDto);

    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "categoryIds", ignore = true)
    ProductDto toDto(Product product);

    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateFromDto(ProductRequestDto requestDto, @MappingTarget Product product);

    @AfterMapping
    default void setCategoryIds(@MappingTarget ProductDto productDto, Product product) {
        if (product.getCategories() != null) {
            Set<Long> categoryIds = product.getCategories().stream()
                    .map(ProductCategory::getId)
                    .collect(Collectors.toSet());
            productDto.setCategoryIds(categoryIds);
        }
    }
}
