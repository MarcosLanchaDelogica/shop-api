package com.example.shop.mappers.order;

import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.domain.model.OrderItem;
import com.example.shop.dto.order.in.OrderStatusUpdateRequest;
import com.example.shop.mappers.common.EnumMapper;
import com.example.shop.mappers.config.Converters;
import com.example.shop.mappers.config.MapStructConfig;
import com.example.shop.domain.model.Order;
import com.example.shop.dto.order.in.OrderCreateRequest;
import com.example.shop.dto.order.out.OrderResponse;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class, uses =  {Converters.class, EnumMapper.class, OrderItemMapper.class, OrderItemViewMapper.class})
public interface OrderMapper {

    // Mapeo de DTO a Order
    //Ignoro los campos que son generados automáticamente o que se establecen en el servicio
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", source = "customerId", qualifiedByName = "idToCustomer")
    @Mapping(target = "shippingAddress", source = "shippingAddressId", qualifiedByName = "idToAddress")
    @Mapping(target = "items", source = "items") // usa OrderItemMapper
    @Mapping(target = "total", ignore = true) // se calcula en servicio
    @Mapping(target = "orderDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "CREATED")
    Order toEntity(OrderCreateRequest request);

    // Mapeo de Order a DTO
    @Mapping(target = "id", source = "id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "shippingAddressId", source = "shippingAddress.id")
    @Mapping(target = "items", source = "items") // usa OrderItemViewMapper
    @Mapping(target = "status", source = "status", qualifiedByName = "orderStatusToString")
    OrderResponse toResponse(Order order);

    // Actualización parcial del estado de la orden
    @BeanMapping(ignoreByDefault = true) //Evito sobrescribir campos no deseados
    @Mapping(target="status", source="status", qualifiedByName="stringToOrderStatus") // conversión de String a Enum en EnumMapper
    void updateStatus(OrderStatusUpdateRequest dto, @MappingTarget Order entity); // Solo actualizo el estado

}
