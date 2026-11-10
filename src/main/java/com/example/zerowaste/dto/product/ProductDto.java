package com.example.zerowaste.dto.product;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDto {

    private Long id;
    private String title;
    private BigDecimal price;
    private String description;
    private Long sellerId;
    private int quantity;
    private Set<Long> categoryIds;
}
