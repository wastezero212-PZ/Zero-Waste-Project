
package com.example.zerowaste.controller;

import com.example.zerowaste.dto.category.ProductCategoryRequestDto;
import com.example.zerowaste.dto.category.ProductCategoryResponseDto;
import com.example.zerowaste.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Category Controller", description = "Endpoints for managing categories")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new category")
    public ProductCategoryResponseDto createCategory(
            @Valid @RequestBody ProductCategoryRequestDto requestDto) {
        return categoryService.save(requestDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all categories")
    public Page<ProductCategoryResponseDto> getAllCategories(Pageable pageable) {
        return categoryService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get category by id")
    public ProductCategoryResponseDto getCategoryById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update category by id")
    public ProductCategoryResponseDto updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategoryRequestDto requestDto) {
        return categoryService.updateById(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete category by id")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
    }
}
