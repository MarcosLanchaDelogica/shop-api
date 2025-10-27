package com.example.shop.dto.order.in;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusUpdateRequest(
        @NotBlank
        String status
) {}
