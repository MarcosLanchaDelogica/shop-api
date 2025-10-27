package com.example.shop.mappers.common;

import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.mappers.config.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

// Mapper para conversiones entre enums y strings
@Mapper(config = MapStructConfig.class)
public interface EnumMapper {
    // Conversión de OrderStatus a String
    @Named("orderStatusToString")
    default String orderStatusToString(OrderStatus status) {
        return status == null ? null : status.name();
    }

    // Conversión de String a OrderStatus, ignorando mayúsculas/minúsculas y espacios
    @Named("stringToOrderStatus")
    default OrderStatus stringToOrderStatus(String status) {
        if (status == null) return null;
        return OrderStatus.valueOf(status.trim().toUpperCase());
    }}
