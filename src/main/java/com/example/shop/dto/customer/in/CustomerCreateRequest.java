package com.example.shop.dto.customer.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerCreateRequest(
        @NotBlank
        @Size(max = 120)
        String fullName,

        @NotBlank
        @Email
        @Size(max = 160)
        String email,

        @NotBlank(message = "El número de teléfono no puede estar vacío")
        @Size(max = 25)
        @Pattern(
                regexp = "^\\+[0-9]{9,15}$",
                message = "El número de teléfono debe tener entre 9 y 15 dígitos, comenzando con '+'"
        )
        String phone
) {}
