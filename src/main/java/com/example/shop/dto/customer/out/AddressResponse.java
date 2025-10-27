package com.example.shop.dto.customer.out;

public record AddressResponse(
        Long id,
        Long customerId,
        String line1,
        String line2,
        String city,
        String postalCode,
        String country,
        boolean isDefault
) {}
