package com.example.shop.dto.product.in;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank
        @Size(max = 40)
        String sku,

        @NotBlank
        @Size(max = 160)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
        BigDecimal price,

        @NotNull
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull
        Boolean active
) {}
