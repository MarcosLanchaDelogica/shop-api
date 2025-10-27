package com.example.shop.dto.customer.in;

import jakarta.validation.constraints.*;

public record CustomerUpdateRequest(

        @NotBlank
        @Size(max = 120)
        String fullName,

        @NotBlank
        @Email
        @Size(max = 160)
        String email,

        @Size(max = 25)
        String phone

) {}
