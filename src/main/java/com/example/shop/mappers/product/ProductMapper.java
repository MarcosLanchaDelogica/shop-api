package com.example.shop.mappers.product;

import com.example.shop.dto.product.in.ProductUpdateRequest;
import com.example.shop.mappers.config.MapStructConfig;
import com.example.shop.domain.model.Product;
import com.example.shop.dto.product.in.ProductCreateRequest;
import com.example.shop.dto.product.out.ProductResponse;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface ProductMapper {

    //Mapeo de DTO a Product
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    //Ya generados automáticamente así que los ignoro
    Product toEntity(ProductCreateRequest dto);

    //Mapeo de Product a DTO
    //Lo mapea bien mapstruct ya que coinciden nombres
    ProductResponse toResponse(Product entity);

    //Actualización parcial de Product a partir del DTO
    @BeanMapping(ignoreByDefault = true) //Evita sobrescribir campos no deseados
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "stock", source = "stock")
    @Mapping(target = "active", source = "active")
    void update(ProductUpdateRequest in, @MappingTarget Product entity); // Actualiza el entity con los datos del DTO
}
