
package com.example.zerowaste.mapper;

import com.example.zerowaste.config.MapperConfig;
import com.example.zerowaste.dto.product.ProductRequestDto;
import com.example.zerowaste.dto.product.ProductResponseDto;
import com.example.zerowaste.model.Product;
import com.example.zerowaste.model.ProductCategory;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface ProductMapper {

    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "categories", ignore = true)
    Product toEntity(ProductRequestDto requestDto);

    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "categoryIds", source = "categories")
    ProductResponseDto toDto(Product product);

    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateFromDto(
            ProductRequestDto requestDto,
            @MappingTarget Product product);

    default Set<Long> mapCategoryIds(Set<ProductCategory> categories) {
        if (categories == null) {
            return Set.of();
        }
        return categories.stream()
                .map(ProductCategory::getId)
                .collect(Collectors.toSet());
    }
}
