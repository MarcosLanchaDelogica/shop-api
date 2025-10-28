package com.example.shop.service;

import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.domain.model.Order;
import com.example.shop.domain.model.OrderItem;
import com.example.shop.Utils.rules.OrderStatusTransitionValidator;
import com.example.shop.Utils.calculations.OrderCalculator;
import com.example.shop.Utils.calculations.StockCalculator;
import com.example.shop.dto.order.in.OrderCreateRequest;
import com.example.shop.dto.order.in.OrderStatusUpdateRequest;
import com.example.shop.dto.order.out.OrderItemViewResponse;
import com.example.shop.dto.order.out.OrderResponse;
import com.example.shop.mappers.order.OrderItemMapper;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusTransitionValidator transitionValidator;
    private final OrderCalculator orderCalculator;
    private final StockCalculator stockCalculator;

    // Crear un nuevo pedido a partir del DTO de entrada.
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest dto) {
        log.info("Creando pedido para cliente {}", dto.customerId());

        // Verifico que el cliente exista
        var customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cliente no encontrado ID " + dto.customerId()));

        // Verifico que la dirección exista y pertenece al cliente
        var address = customer.getAddresses().stream()
                .filter(a -> a.getId().equals(dto.shippingAddressId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dirección no encontrada o no pertenece al cliente"));

        //Creo el pedido base
        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        //Procesar las líneas del pedido
        for (var itemDto : dto.items()) {
            var product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto no encontrado ID " + itemDto.productId()));

            // Validación y actualización de stock
            stockCalculator.validateStock(product, itemDto.quantity());
            stockCalculator.reduceStock(product, itemDto.quantity());

            // Creación de línea
            OrderItem item = orderItemMapper.toEntity(itemDto);
            item.setOrder(order);
            item.setProduct(product);
            item.setUnitPrice(product.getPrice());

            order.getItems().add(item);
        }

        // Cálculo del total
        var total = orderCalculator.calculateOrderTotal(order.getItems());
        order.setTotal(total);

        var saved = orderRepository.saveAndFlush(order);
        return buildOrderResponse(saved);

    }

    //Listar pedidos con filtros opcionales y paginación.
    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(Optional<Long> customerId,
                                          Optional<LocalDate> fromDate,
                                          Optional<LocalDate> toDate,
                                          Optional<OrderStatus> status,
                                          Pageable pageable) {

        Specification<Order> spec = Specification.allOf();

        if (customerId.isPresent()) {
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("customer").get("id"), customerId.get()));
        }
        if (fromDate.isPresent()) {
            spec = spec.and((root, q, cb) ->
                    cb.greaterThanOrEqualTo(root.get("orderDate"), fromDate.get().atStartOfDay()));
        }
        if (toDate.isPresent()) {
            spec = spec.and((root, q, cb) ->
                    cb.lessThanOrEqualTo(root.get("orderDate"), toDate.get().atTime(LocalTime.MAX)));
        }
        if (status.isPresent()) {
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("status"), status.get()));
        }

        return orderRepository.findAll(spec, pageable)
                .map(this::buildOrderResponse);
    }

    //Obtener un pedido por su ID.
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        return buildOrderResponse(order);
    }

    //Actualizar el estado de un pedido con validación de transición.
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        var current = order.getStatus();
        OrderStatus next;

        try {
            next = OrderStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no válido: " + request.status());
        }

        //Validar transición de estado
        if (!transitionValidator.isValidTransition(current, next)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transición no permitida: " + current + " -> " + next);
        }

        log.info("Actualizando estado del pedido ID {} de {} a {}", id, current, next);
        order.setStatus(next);

        //Si el pedido pasa a CANCELLED, devolvemos stock de todos los productos
        if (next == OrderStatus.CANCELLED) {
            log.info("Pedido {} cancelado. Devolviendo stock de productos asociados...", id);
            for (var item : order.getItems()) {
                var product = item.getProduct();
                stockCalculator.increaseStock(product, item.getQuantity());
                log.debug("Devuelto stock: +{} unidades al producto ID {}", item.getQuantity(), product.getId());
            }
        }

        var saved = orderRepository.save(order);
        log.debug("Estado final del pedido {}: {}", id, saved.getStatus());

        return buildOrderResponse(saved);
    }


    // Método auxiliar para construir la respuesta del pedido.
    public OrderResponse buildOrderResponse(Order order) {
        var enrichedItems = order.getItems().stream()
                .map(item -> new OrderItemViewResponse(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        orderCalculator.calculateLineTotal(item.getProduct(), item.getQuantity())
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderDate(),
                order.getStatus().name(),
                order.getCustomer().getId(),
                order.getShippingAddress().getId(),
                enrichedItems,
                order.getTotal()
        );
    }

}
