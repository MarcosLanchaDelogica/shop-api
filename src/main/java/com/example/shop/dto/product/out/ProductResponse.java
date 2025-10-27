package com.example.shop.dto.product.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
