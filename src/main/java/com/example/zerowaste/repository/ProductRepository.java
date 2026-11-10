package com.example.zerowaste.repository;

import com.example.zerowaste.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategories_Id(Long categoryId, Pageable pageable);

    Page<Product> findBySeller_Id(Long sellerId, Pageable pageable);
}
