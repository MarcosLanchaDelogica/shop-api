package com.example.shop.mappers.config;

import com.example.shop.domain.model.Address;
import com.example.shop.domain.model.Customer;
import com.example.shop.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

//Clase con convertidores personalizados para MapStruct.
@Mapper(componentModel = "spring")
public interface Converters {

    //Convertir un ID de DTO en una entidad Customer.
    @Named("idToCustomer")
    default Customer idToCustomer(Long id) {
        if (id == null) return null;
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }

    //Convertir un ID de DTO en una entidad Address.
    @Named("idToAddress")
    default Address idToAddress(Long id) {
        if (id == null) return null;
        Address address = new Address();
        address.setId(id);
        return address;
    }

    //Convertir un ID de DTO en una entidad Product.
    @Named("idToProduct")
    default Product idToProduct(Long id) {
        if (id == null) return null;
        Product product = new Product();
        product.setId(id);
        return product;
    }


}
