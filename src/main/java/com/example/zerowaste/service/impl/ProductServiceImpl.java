
package com.example.zerowaste.service.impl;

import com.example.zerowaste.dto.product.ProductRequestDto;
import com.example.zerowaste.dto.product.ProductResponseDto;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.mapper.ProductMapper;
import com.example.zerowaste.model.Product;
import com.example.zerowaste.model.ProductCategory;
import com.example.zerowaste.model.User;
import com.example.zerowaste.repository.ProductCategoryRepository;
import com.example.zerowaste.repository.ProductRepository;
import com.example.zerowaste.repository.UserRepository;
import com.example.zerowaste.service.ProductService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponseDto save(ProductRequestDto requestDto, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cannot find seller: " + sellerEmail));

        Product product = productMapper.toEntity(requestDto);
        product.setSeller(seller);
        product.setCategories(findCategories(requestDto.getCategoryIds()));

        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    public Page<ProductResponseDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toDto);
    }

    @Override
    public Page<ProductResponseDto> findByCategoryId(
            Long categoryId, Pageable pageable) {
        return productRepository.findByCategories_Id(categoryId, pageable)
                .map(productMapper::toDto);
    }

    @Override
    public Page<ProductResponseDto> findBySellerId(
            Long sellerId, Pageable pageable) {
        return productRepository.findBySeller_Id(sellerId, pageable)
                .map(productMapper::toDto);
    }

    @Override
    public ProductResponseDto getById(Long id) {
        return productMapper.toDto(findProduct(id));
    }

    @Override
    @Transactional
    public ProductResponseDto updateById(
            Long id, ProductRequestDto requestDto, String sellerEmail) {
        Product product = findProduct(id);
        checkOwner(product, sellerEmail);

        productMapper.updateFromDto(requestDto, product);
        product.setCategories(findCategories(requestDto.getCategoryIds()));

        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteById(Long id, String sellerEmail) {
        Product product = findProduct(id);
        checkOwner(product, sellerEmail);
        productRepository.delete(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cannot find product with id: " + id));
    }

    private Set<ProductCategory> findCategories(Set<Long> categoryIds) {
        Set<ProductCategory> categories = new HashSet<>();

        if (categoryIds == null) {
            return categories;
        }

        for (Long categoryId : categoryIds) {
            ProductCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Cannot find category with id: " + categoryId));
            categories.add(category);
        }

        return categories;
    }

    private void checkOwner(Product product, String sellerEmail) {
        if (!product.getSeller().getEmail().equalsIgnoreCase(sellerEmail)) {
            throw new AccessDeniedException(
                    "You cannot modify another user's product");
        }
    }
}
