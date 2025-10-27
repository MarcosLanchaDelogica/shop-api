package com.example.shop.exceptions;

import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Builder
public record ApiError(
        Instant timestamp,
        String path,
        int status,
        String error,
        String code,
        String message,
        List<FieldApiError> details,
        String traceId
) {}
