package com.example.shop.mappers.customer;

import com.example.shop.dto.customer.in.CustomerUpdateRequest;
import com.example.shop.mappers.config.Converters;
import com.example.shop.mappers.config.MapStructConfig;
import com.example.shop.domain.model.Customer;
import com.example.shop.dto.customer.in.CustomerCreateRequest;
import com.example.shop.dto.customer.out.CustomerResponse;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class, uses = {Converters.class})
public interface CustomerMapper {

    // Mapea CustomerCreateRequest a Customer, ignorando campos gestionados por la base de datos
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerCreateRequest dto);

    // Mapea Customer a CustomerResponse, ignorando las direcciones porque se gestionan aparte
    @BeanMapping(ignoreUnmappedSourceProperties = {"addresses"})
    CustomerResponse toResponse(Customer entity);

    // Actualiza una entidad Customer existente con datos de CustomerUpdateRequest
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(CustomerUpdateRequest dto, @MappingTarget Customer entity);

}
