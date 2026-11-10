
package com.example.zerowaste.dto.product;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDto {

    private Long id;
    private String title;
    private BigDecimal price;
    private String description;
    private Long sellerId;
    private Set<Long> categoryIds;
}
