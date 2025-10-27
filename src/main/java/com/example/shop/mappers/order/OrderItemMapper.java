package com.example.shop.mappers.order;

import com.example.shop.dto.order.in.OrderItemViewRequest;
import com.example.shop.mappers.config.MapStructConfig;
import com.example.shop.mappers.config.Converters;
import com.example.shop.domain.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = { Converters.class })
public interface OrderItemMapper {

    // Mapea OrderItemViewRequest a OrderItem
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", source = "productId", qualifiedByName = "idToProduct")
    @Mapping(target = "unitPrice", ignore = true) // se calculará en servicio
    OrderItem toEntity(OrderItemViewRequest dto);
}
