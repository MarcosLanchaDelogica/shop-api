package com.example.shop.mappers.order;

import com.example.shop.dto.order.out.OrderItemViewResponse;
import com.example.shop.mappers.config.MapStructConfig;
import com.example.shop.domain.model.OrderItem;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface OrderItemViewMapper {

    // Mapea OrderItem a OrderItemViewResponse
    @BeanMapping(ignoreUnmappedSourceProperties = { "id", "order" }) // ignorar propiedades no mapeadas
    @Mapping(target = "productId",  source = "product.id")
    @Mapping(target = "lineTotal", ignore = true) // se calculará en el servicio
    OrderItemViewResponse toView(OrderItem entity);
}
