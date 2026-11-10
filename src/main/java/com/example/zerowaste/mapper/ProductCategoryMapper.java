package com.example.zerowaste.mapper;

import com.example.zerowaste.config.MapperConfig;
import com.example.zerowaste.dto.category.ProductCategoryDto;
import com.example.zerowaste.dto.category.ProductCategoryRequestDto;
import com.example.zerowaste.model.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface ProductCategoryMapper {

    ProductCategory toEntity(ProductCategoryRequestDto requestDto);

    ProductCategoryDto toDto(ProductCategory category);

    void updateFromDto(
            ProductCategoryRequestDto requestDto,
            @MappingTarget ProductCategory category);
}
