package com.example.shop.controller;

import com.example.shop.dto.product.in.ProductCreateRequest;
import com.example.shop.dto.product.in.ProductUpdateRequest;
import com.example.shop.dto.product.out.ProductResponse;
import com.example.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

/**
 * Controlador REST para la gestión del catálogo de productos.
 *
 * <p>Permite:
 * <ul>
 *     <li>Crear, listar, actualizar y eliminar productos</li>
 *     <li>Filtrar por nombre y estado activo</li>
 *     <li>Listar resultados con paginación y ordenación</li>
 * </ul>
 *
 * Endpoint base: {@code /api/products}
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * POST /api/products
     *
     * Crea un nuevo producto en el catálogo.
     *
     * @param req DTO con los datos de creación.
     * @return Producto creado (HTTP 201 Created)
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest req) {
        ProductResponse response = productService.createProduct(req);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * GET /api/products
     *
     * Lista los productos con filtros opcionales y paginación.
     * Ejemplo: {@code /api/products?name=camisa&active=true&page=0&size=20&sort=name,asc}
     *
     * @param name filtro opcional por nombre parcial del producto.
     * @param active filtro opcional por estado activo/inactivo.
     * @param pageable parámetros de paginación (page, size, sort).
     * @return Página de productos filtrados (HTTP 200 OK)
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var page = productService.getAllFilteredProducts(
                Optional.ofNullable(name),
                Optional.ofNullable(active),
                pageable
        );
        return ResponseEntity.ok(page);
    }

    /**
     * GET /api/products/{id}
     *
     * Devuelve el detalle completo de un producto.
     *
     * @param id identificador del producto.
     * @return Detalle del producto (HTTP 200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    /**
     * PUT /api/products/{id}
     *
     * Actualiza los datos de un producto existente.
     *
     * @param id identificador del producto a actualizar.
     * @param req DTO con los nuevos valores de los campos editables.
     * @return Producto actualizado (HTTP 200 OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest req
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, req));
    }

    /**
     * DELETE /api/products/{id}
     *
     * Elimina un producto del catálogo.
     * Puede ser baja lógica (marcar inactivo) o física (eliminar registro),
     * según la implementación en el servicio.
     *
     * @param id identificador del producto.
     * @return HTTP 204 No Content si se eliminó correctamente.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean soft) {
        // Por defecto, baja lógica (soft delete = true)
        productService.delete(id, soft);
        return ResponseEntity.noContent().build();
    }
}
