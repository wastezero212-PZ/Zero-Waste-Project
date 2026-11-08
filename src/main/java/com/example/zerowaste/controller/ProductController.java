
package com.example.zerowaste.controller;

import com.example.zerowaste.dto.product.ProductRequestDto;
import com.example.zerowaste.dto.product.ProductResponseDto;
import com.example.zerowaste.model.User;
import com.example.zerowaste.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product Controller", description = "Endpoints for managing products")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new product")
    public ProductResponseDto createProduct(
            @Valid @RequestBody ProductRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        return productService.save(requestDto, user.getEmail());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all products")
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get product by id")
    public ProductResponseDto getProductById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/category/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get products by category")
    public Page<ProductResponseDto> getProductsByCategory(
            @PathVariable Long id, Pageable pageable) {
        return productService.findByCategoryId(id, pageable);
    }

    @GetMapping("/uzytkownik/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get products by seller")
    public Page<ProductResponseDto> getProductsBySeller(
            @PathVariable Long id, Pageable pageable) {
        return productService.findBySellerId(id, pageable);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update product by id")
    public ProductResponseDto updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        return productService.updateById(id, requestDto, user.getEmail());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product by id")
    public void deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        productService.deleteById(id, user.getEmail());
    }
}
