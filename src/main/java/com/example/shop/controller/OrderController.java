package com.example.shop.controller;

import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.dto.order.in.OrderCreateRequest;
import com.example.shop.dto.order.in.OrderStatusUpdateRequest;
import com.example.shop.dto.order.out.OrderResponse;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

/*
    * Controlador REST para la gestión de pedidos.
    * Permite:
    * - Crear pedidos con líneas y calcular totales
    * - Listar pedidos con filtros y paginación
    * - Consultar detalle completo de un pedido
    * - Cambiar estado del pedido con transiciones válidas
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders
     * Crea un pedido con sus líneas, calcula totales y estado inicial CREATED
     *
     * @param req DTO con los datos de creación del pedido
     * @return Pedido creado (HTTP 201 Created)
     *
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest req) {
        log.info("POST /api/orders - creando pedido");
        OrderResponse response = orderService.createOrder(req);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * GET /api/orders
     * Lista pedidos con filtros opcionales y paginación
     * @param customerId filtro opcional por ID de cliente
     * @param fromDate filtro opcional por fecha mínima de pedido
     * @param toDate filtro opcional por fecha máxima de pedido
     * @param status filtro opcional por estado del pedido
     * @param pageable parámetros de paginación y ordenación
     * @return Página de pedidos filtrados (HTTP 200 OK)
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/orders - listando pedidos con filtros");
        Page<OrderResponse> page = orderService.listOrders(
                Optional.ofNullable(customerId),
                Optional.ofNullable(fromDate),
                Optional.ofNullable(toDate),
                Optional.ofNullable(status),
                pageable
        );
        return ResponseEntity.ok(page);
    }

    /**
     * GET /api/orders/{id}
     * Devuelve el detalle completo de un pedido
     * @param id identificador del pedido
     * @return Detalle del pedido (HTTP 200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        log.info("GET /api/orders/{} - obteniendo detalle", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * PUT /api/orders/{id}/status
     * Cambia el estado del pedido siguiendo transiciones válidas
     * @param id identificador del pedido
     * @param req DTO con el nuevo estado
     * @return Pedido actualizado (HTTP 200 OK)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody OrderStatusUpdateRequest req) {
        log.info("PUT /api/orders/{}/status - nuevo estado {}", id, req.status());
        return ResponseEntity.ok(orderService.updateOrderStatus(id, req));
    }
}
