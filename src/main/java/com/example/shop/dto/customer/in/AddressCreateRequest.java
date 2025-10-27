package com.example.shop.dto.customer.in;

import jakarta.validation.constraints.*;

public record AddressCreateRequest(
        Long customerId,

        @NotBlank
        @Size(max = 160)
        String line1,

        @Size(max = 160)
        String line2,

        @NotBlank
        @Size(max = 80)
        String city,

        @NotBlank
        @Size(max = 20)
        String postalCode,

        @NotBlank
        @Size(max = 80)
        String country,

        @NotNull
        Boolean isDefault

) {}
