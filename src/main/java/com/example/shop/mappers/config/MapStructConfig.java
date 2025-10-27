package com.example.shop.mappers.config;

import org.mapstruct.*;
import org.mapstruct.control.DeepClone;

// Configuración común para todos los mappers MapStruct de la aplicación
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,      // inyección vía Spring
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,            // preferimos constructor injection
        unmappedTargetPolicy = ReportingPolicy.ERROR,                 // si falta mapear un destino, que falle
        unmappedSourcePolicy = ReportingPolicy.ERROR,                 // si sobra un source no usado, que avise/falle
        typeConversionPolicy = ReportingPolicy.ERROR,                 // conversiones implícitas “raras” no permitidas
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, // útil para entidades JPA con addItem(...)
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,       // siempre comprobar nulls antes de mapear
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, // en updates, no pisar con null
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT, // listas/maps nulos → colecciones vacías
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG, // permite heredar config común
        mappingControl = DeepClone.class // evita compartir referencias mutables por accidente
)
public interface MapStructConfig {}
