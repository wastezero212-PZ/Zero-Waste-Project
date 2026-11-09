
package com.example.zerowaste.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDto {

    @NotBlank
    private String title;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;

    private String description;

    private Set<Long> categoryIds = new HashSet<>();
}
