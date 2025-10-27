package com.example.shop.dto.order.in;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemViewRequest(
        @NotNull
        Long productId,

        @Min(value = 1, message = "La cantidad debe ser mayor que 0.")
        int quantity

) {}
