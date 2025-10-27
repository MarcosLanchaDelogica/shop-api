package com.example.shop.controller;

import com.example.shop.dto.common.PagedResponse;
import com.example.shop.dto.customer.in.CustomerCreateRequest;
import com.example.shop.dto.customer.in.CustomerUpdateRequest;
import com.example.shop.dto.customer.in.AddressCreateRequest;
import com.example.shop.dto.customer.out.CustomerResponse;
import com.example.shop.dto.customer.out.AddressResponse;
import com.example.shop.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de clientes y sus direcciones.
 *
 * Permite:
 *
 *     Crear, consultar, actualizar y eliminar clientes
 *     Listar clientes con paginación y filtro por email
 *     Gestionar direcciones asociadas, incluida la dirección predeterminada
 *
 *
 * Endpoint base: {@code /api/customers}
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * POST /api/customers
     *
     * Crea un nuevo cliente en el sistema.
     *
     * @param req DTO con los datos de creación del cliente.
     * @return Cliente creado (HTTP 201 Created)
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest req) {
        CustomerResponse response = customerService.createCustomer(req);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * GET /api/customers
     *
     * Lista los clientes registrados con paginación y filtro opcional por email.
     * Ejemplo: {@code /api/customers?page=0&size=10&email=juan@correo.com}
     *
     * @param email filtro opcional por correo electrónico.
     * @param pageable parámetros de paginación y ordenación.
     * @return Página de clientes (HTTP 200 OK)
     */
    @GetMapping
    public ResponseEntity<PagedResponse<CustomerResponse>> getAllCustomers(
            @RequestParam(name = "email", required = false) String email,
            @ParameterObject
            @PageableDefault(sort = "fullName", direction = Sort.Direction.ASC, size = 10)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                customerService.getAllCustomers(pageable, Optional.ofNullable(email))
        );
    }

    /**
     * GET /api/customers/{id}
     *
     * Obtiene el detalle completo de un cliente por su ID.
     *
     * @param id identificador del cliente.
     * @return Datos del cliente (HTTP 200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    /**
     * PUT /api/customers/{id}
     *
     * Actualiza los datos de un cliente existente.
     *
     * @param id identificador del cliente a actualizar.
     * @param req DTO con los campos modificables.
     * @return Cliente actualizado (HTTP 200 OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest req
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(id, req));
    }

    /**
     * DELETE /api/customers/{id}
     *
     * Elimina un cliente del sistema.
     * Se devuelve un 204 sin cuerpo si la eliminación fue exitosa.
     *
     * @param id identificador del cliente.
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/customers/{id}/addresses
     *
     * Crea una nueva dirección asociada a un cliente.
     *
     * @param id identificador del cliente.
     * @param req DTO con los datos de la dirección.
     * @return Lista actualizada de direcciones (HTTP 200 OK)
     */
    @PostMapping("/{id}/addresses")
    public ResponseEntity<List<AddressResponse>> createAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressCreateRequest req
    ) {
        return ResponseEntity.ok(customerService.addAddress(id, req));
    }

    /**
     * PUT /api/customers/{id}/addresses/{addressId}/default
     *
     * Marca una dirección específica como predeterminada.
     *
     * @param id identificador del cliente.
     * @param addressId identificador de la dirección a marcar.
     * @return Dirección actualizada como predeterminada (HTTP 200 OK)
     */
    @PutMapping("/{id}/addresses/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @PathVariable Long id,
            @PathVariable Long addressId
    ) {
        return ResponseEntity.ok(customerService.setDefaultAddress(id, addressId));
    }
}
