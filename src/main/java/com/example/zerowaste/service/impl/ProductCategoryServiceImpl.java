package com.example.zerowaste.service.impl;

import com.example.zerowaste.dto.category.ProductCategoryDto;
import com.example.zerowaste.dto.category.ProductCategoryRequestDto;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.mapper.ProductCategoryMapper;
import com.example.zerowaste.model.ProductCategory;
import com.example.zerowaste.repository.ProductCategoryRepository;
import com.example.zerowaste.service.ProductCategoryService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductCategoryMapper categoryMapper;

    @Override
    public ProductCategoryDto save(ProductCategoryRequestDto requestDto) {
        ProductCategory category = categoryMapper.toEntity(requestDto);
        String name = normalizeName(category.getName());

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category already exists: " + name);
        }

        category.setName(name);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public Page<ProductCategoryDto> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toDto);
    }

    @Override
    public ProductCategoryDto getById(Long id) {
        return categoryMapper.toDto(findCategory(id));
    }

    @Override
    public ProductCategoryDto updateById(
            Long id, ProductCategoryRequestDto requestDto) {
        ProductCategory category = findCategory(id);
        categoryMapper.updateFromDto(requestDto, category);
        String name = normalizeName(category.getName());

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category already exists: " + name);
        }

        category.setName(name);
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

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Category name cannot be empty");
        }

        return name.trim().toLowerCase(Locale.ROOT);
    }
}
