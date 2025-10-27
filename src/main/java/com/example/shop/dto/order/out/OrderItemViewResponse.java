package com.example.shop.dto.order.out;

import java.math.BigDecimal;

public record OrderItemViewResponse(
        Long productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {}
