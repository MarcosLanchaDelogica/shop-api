package com.example.shop.dto.order.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        LocalDateTime orderDate,
        String status,
        Long customerId,
        Long shippingAddressId,
        List<OrderItemViewResponse> items,
        BigDecimal total

) {}
