
package com.example.zerowaste.service.impl;

import com.example.zerowaste.dto.category.ProductCategoryRequestDto;
import com.example.zerowaste.dto.category.ProductCategoryResponseDto;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.mapper.ProductCategoryMapper;
import com.example.zerowaste.model.ProductCategory;
import com.example.zerowaste.repository.ProductCategoryRepository;
import com.example.zerowaste.service.ProductCategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductCategoryMapper categoryMapper;

    @Override
    public ProductCategoryResponseDto save(ProductCategoryRequestDto requestDto) {
        ProductCategory category = categoryMapper.toEntity(requestDto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<ProductCategoryResponseDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public ProductCategoryResponseDto getById(Long id) {
        return categoryMapper.toDto(findCategory(id));
    }

    @Override
    public ProductCategoryResponseDto updateById(
            Long id, ProductCategoryRequestDto requestDto) {
        ProductCategory category = findCategory(id);
        categoryMapper.updateFromDto(requestDto, category);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void deleteById(Long id) {
        ProductCategory category = findCategory(id);
        categoryRepository.delete(category);
    }

    private ProductCategory findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cannot find category with id: " + id));
    }
}
