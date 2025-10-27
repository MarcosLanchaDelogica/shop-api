package com.example.shop.dto.order.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotNull
        Long customerId,

        @NotNull
        Long shippingAddressId,

        @NotEmpty
        List<@Valid OrderItemViewRequest> items
) {}
