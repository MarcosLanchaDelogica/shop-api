package com.example.shop.service;

import com.example.shop.domain.model.Product;
import com.example.shop.dto.product.in.ProductCreateRequest;
import com.example.shop.dto.product.in.ProductUpdateRequest;
import com.example.shop.dto.product.out.ProductResponse;
import com.example.shop.mappers.product.ProductMapper;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.spec.ProductFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // Crear un nuevo producto a partir del DTO de entrada.
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creando producto con SKU {}", request.sku());

        // Compruebo que no exista otro producto con el mismo SKU.
        if (productRepository.existsBySku(request.sku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un producto con SKU " + request.sku());
        }

        // Uso MapStruct para convertir el DTO a entidad.
        Product entity = productMapper.toEntity(request);

        // Si el campo active no viene establecido, lo marco como activo.
        if (request.active() == null) {
            entity.setActive(true);
        }

        // Guardo el producto en la base de datos.
        Product saved = productRepository.save(entity);

        // Devuelvo el producto recién creado en formato DTO.
        return productMapper.toResponse(saved);
    }

    // Obtener una página de productos filtrados por nombre y estado activo.
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllFilteredProducts(Optional<String> nameFilter,
                                                        Optional<Boolean> activeFilter,
                                                        Pageable pagination) {
        // Comienzo con una Specification vacía.
        Specification<Product> spec = Specification.allOf();

        // Si hay filtro por nombre, lo aplico.
        if (nameFilter.isPresent() && !nameFilter.get().isBlank()) {
            spec = spec.and(ProductFilter.nameContains(nameFilter.get()));
        }

        // Si hay filtro por estado activo, también lo aplico.
        if (activeFilter.isPresent()) {
            spec = spec.and(ProductFilter.activeEquals(activeFilter.get()));
        }

        // Consulto la base de datos con la Specification y la paginación.
        Page<ProductResponse> page = productRepository.findAll(spec, pagination)
                .map(productMapper::toResponse);

        // Devuelvo los resultados paginados.
        return page;
    }

    // Obtener un producto por su ID.
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        // Busco el producto o lanzo un 404 si no existe.
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto " + id + " no encontrado"));

        // Devuelvo el DTO correspondiente.
        return productMapper.toResponse(entity);
    }

    // Actualizar un producto existente con los datos del DTO.
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest req) {
        // Busco el producto.
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto " + id + " no encontrado"));

        // MapStruct aplica los cambios sobre la entidad existente.
        productMapper.update(req, entity);

        try {
            Product saved = productRepository.save(entity);
            return productMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Actualización no válida", ex);
        }
    }

    // Eliminar un producto por ID, con opción de baja lógica o física.
    @Transactional
    public void delete(Long id, boolean soft) {
        // Busco el producto.
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto " + id + " no encontrado"));

        // Si la baja es lógica, marco el producto como inactivo.
        if (soft) {
            if (entity.isActive()) {
                entity.setActive(false);
                productRepository.save(entity);
            }
        } else {
            // Si es física, elimino el registro.
            productRepository.delete(entity);
        }
    }
}
