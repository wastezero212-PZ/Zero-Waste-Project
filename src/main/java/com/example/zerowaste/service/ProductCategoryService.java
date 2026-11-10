
package com.example.zerowaste.service;

import com.example.zerowaste.dto.category.ProductCategoryRequestDto;
import com.example.zerowaste.dto.category.ProductCategoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCategoryService {

    ProductCategoryResponseDto save(ProductCategoryRequestDto requestDto);

    Page<ProductCategoryResponseDto> findAll(Pageable pageable);

    ProductCategoryResponseDto getById(Long id);

    ProductCategoryResponseDto updateById(
            Long id, ProductCategoryRequestDto requestDto);

    void deleteById(Long id);
}
