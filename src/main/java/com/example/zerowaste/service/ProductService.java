
package com.example.zerowaste.service;

import com.example.zerowaste.dto.product.ProductRequestDto;
import com.example.zerowaste.dto.product.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDto save(
            ProductRequestDto requestDto, String sellerEmail);

    Page<ProductResponseDto> findAll(Pageable pageable);

    Page<ProductResponseDto> findByCategoryId(
            Long categoryId, Pageable pageable);

    Page<ProductResponseDto> findBySellerId(
            Long sellerId, Pageable pageable);

    ProductResponseDto getById(Long id);

    ProductResponseDto updateById(
            Long id, ProductRequestDto requestDto, String sellerEmail);

    void deleteById(Long id, String sellerEmail);
}
