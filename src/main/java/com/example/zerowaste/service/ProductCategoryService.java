
package com.example.zerowaste.service;

import com.example.zerowaste.dto.category.ProductCategoryRequestDto;
import com.example.zerowaste.dto.category.ProductCategoryResponseDto;
import java.util.List;

public interface ProductCategoryService {

    ProductCategoryResponseDto save(ProductCategoryRequestDto requestDto);

    List<ProductCategoryResponseDto> findAll();

    ProductCategoryResponseDto getById(Long id);

    ProductCategoryResponseDto updateById(
            Long id, ProductCategoryRequestDto requestDto);

    void deleteById(Long id);
}
