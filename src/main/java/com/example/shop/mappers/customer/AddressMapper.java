package com.example.shop.mappers.customer;

import com.example.shop.mappers.config.Converters;
import com.example.shop.mappers.config.MapStructConfig;
import com.example.shop.domain.model.Address;
import com.example.shop.dto.customer.in.AddressCreateRequest;
import com.example.shop.dto.customer.out.AddressResponse;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class, uses = {Converters.class})
public interface AddressMapper {

    // Convertir el DTO de creación en una entidad Address.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", source = "customerId", qualifiedByName = "idToCustomer")
    Address toEntity(AddressCreateRequest dto);

    // Convertir la entidad Address en el DTO de salida.
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "isDefault", source = "default")
    AddressResponse toResponse(Address entity);

}
