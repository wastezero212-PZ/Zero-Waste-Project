package com.example.zerowaste.service;

import com.example.zerowaste.dto.product.ProductDto;
import com.example.zerowaste.dto.product.ProductRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductDto save(ProductRequestDto requestDto, String sellerEmail);

    Page<ProductDto> findAll(Pageable pageable);

    Page<ProductDto> findByCategoryId(Long categoryId, Pageable pageable);

    Page<ProductDto> findBySellerId(Long sellerId, Pageable pageable);

    ProductDto getById(Long id);

    ProductDto updateById(Long id, ProductRequestDto requestDto, String sellerEmail);

    void deleteById(Long id, String sellerEmail);
}
