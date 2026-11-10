package com.example.zerowaste.service;

import com.example.zerowaste.dto.category.ProductCategoryDto;
import com.example.zerowaste.dto.category.ProductCategoryRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCategoryService {

    ProductCategoryDto save(ProductCategoryRequestDto requestDto);

    Page<ProductCategoryDto> findAll(Pageable pageable);

    ProductCategoryDto getById(Long id);

    ProductCategoryDto updateById(Long id, ProductCategoryRequestDto requestDto);

    void deleteById(Long id);
}
