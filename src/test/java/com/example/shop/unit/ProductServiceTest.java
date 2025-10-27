package com.example.shop.unit;

import com.example.shop.domain.model.Product;
import com.example.shop.dto.product.in.ProductCreateRequest;
import com.example.shop.dto.product.in.ProductUpdateRequest;
import com.example.shop.dto.product.out.ProductResponse;
import com.example.shop.mappers.product.ProductMapper;
import com.example.shop.repository.ProductRepository;
import com.example.shop.service.ProductService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock ProductRepository repo;
    @Mock ProductMapper mapper;
    @InjectMocks ProductService service;

    private Product entity;
    private ProductResponse response;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        entity = Product.builder()
                .id(1L).sku("SKU-1").name("Prod 1").description("desc")
                .price(BigDecimal.TEN).stock(5).active(true)
                .build();
        response = new ProductResponse(1L, "SKU-1", "Prod 1", "desc",
                BigDecimal.TEN, 5, true, null, null);
    }

    //Creación
    @Test
    void createProduct_shouldHandleValidAndInvalidScenarios() {
        var req = new ProductCreateRequest("SKU-1", "Prod 1", "desc", BigDecimal.TEN, 5, null);

        // Caso feliz
        when(repo.existsBySku("SKU-1")).thenReturn(false);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        var out = service.createProduct(req);
        assertEquals("SKU-1", out.sku());
        assertTrue(entity.isActive());
        verify(repo).save(entity);

        // SKU duplicado
        when(repo.existsBySku("SKU-1")).thenReturn(true);
        var ex = assertThrows(ResponseStatusException.class, () -> service.createProduct(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    //Consulta
    @Test
    void getProduct_shouldReturnOrThrow() {
        when(repo.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertEquals(1L, service.getProduct(1L).id());
        verify(repo).findById(1L);

        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.getProduct(99L));
    }

    //Listado
    @Test
    void getAllFilteredProducts_shouldWorkForAllFilterCombinations() {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(entity), pageable, 1);
        when(repo.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toResponse(entity)).thenReturn(response);

        var filters = List.of(
                service.getAllFilteredProducts(Optional.of("name"), Optional.of(true), pageable),
                service.getAllFilteredProducts(Optional.of("   "), Optional.of(true), pageable),
                service.getAllFilteredProducts(Optional.empty(), Optional.empty(), pageable)
        );

        filters.forEach(p -> assertEquals(1, p.getTotalElements()));
        verify(repo, atLeastOnce()).findAll(any(Specification.class), eq(pageable));
    }

    //Actualización
    @Test
    void updateProduct_shouldSaveOrThrow() {
        var req = new ProductUpdateRequest("Prod 2", "desc", BigDecimal.ONE, 1, true);
        when(repo.findById(1L)).thenReturn(Optional.of(entity));
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertNotNull(service.updateProduct(1L, req));
        verify(repo).save(entity);

        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.updateProduct(999L, req));

        when(repo.findById(1L)).thenReturn(Optional.of(entity));
        when(repo.save(entity)).thenThrow(new DataIntegrityViolationException("fail"));
        assertThrows(ResponseStatusException.class, () -> service.updateProduct(1L, req));
    }

    //Eliminación
    @Test
    void deleteProduct_shouldHandleSoftAndHardDeletes() {
        when(repo.findById(1L)).thenReturn(Optional.of(entity));

        // Soft delete activo
        service.delete(1L, true);
        assertFalse(entity.isActive());
        verify(repo).save(entity);

        // Soft delete ya inactivo -> no guarda
        entity.setActive(false);
        service.delete(1L, true);
        verify(repo, times(1)).save(entity); // solo la primera vez

        // Hard delete
        when(repo.findById(2L)).thenReturn(Optional.of(entity));
        service.delete(2L, false);
        verify(repo).delete(entity);

        // Not found
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.delete(99L, true));
    }
}
