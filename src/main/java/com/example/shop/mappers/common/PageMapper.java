package com.example.shop.mappers.common;

import com.example.shop.dto.common.PagedResponse;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

// Mapper para convertir Page<T> de Spring Data en PagedResponse<T> personalizado.
@Mapper(componentModel = "spring")
public interface PageMapper {
    default <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
