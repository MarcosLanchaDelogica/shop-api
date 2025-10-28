package com.example.shop.unit;

import com.example.shop.Utils.calculations.OrderCalculator;
import com.example.shop.Utils.calculations.StockCalculator;
import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.domain.model.*;
import com.example.shop.domain.model.Order;
import com.example.shop.Utils.rules.OrderStatusTransitionValidator;
import com.example.shop.dto.order.in.*;
import com.example.shop.dto.order.out.OrderResponse;
import com.example.shop.mappers.order.*;
import com.example.shop.repository.*;
import com.example.shop.service.OrderService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock OrderRepository orderRepo;
    @Mock ProductRepository productRepo;
    @Mock CustomerRepository customerRepo;
    @Mock OrderMapper orderMapper;
    @Mock OrderItemMapper orderItemMapper;
    @Mock OrderStatusTransitionValidator transitionValidator;
    @Mock StockCalculator stockCalc;
    @Mock OrderCalculator orderCalc;

    @InjectMocks OrderService service;

    private Customer customer;
    private Product product;
    private Order order;
    private OrderResponse response;

    private static final Pageable PAGE = PageRequest.of(0, 10);

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        customer = new Customer();
        customer.setId(1L);
        var address = new Address();
        address.setId(1L);
        address.setCustomer(customer);
        customer.setAddresses(List.of(address));

        product = Product.builder()
                .id(10L).sku("SKU").name("P").price(new BigDecimal("5.00"))
                .stock(10).active(true).build();

        order = new Order();
        order.setId(100L);
        order.setCustomer(customer);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.CREATED);
        order.setItems(new ArrayList<>());
        order.setTotal(BigDecimal.ZERO);

        response = new OrderResponse(100L, LocalDateTime.now(), "CREATED", 1L, 1L, List.of(), BigDecimal.ZERO);
    }

    //Creación
    @Test
    void createOrder_shouldHandleValidAndInvalidScenarios() {
        var validReq = new OrderCreateRequest(1L, 1L, List.of(new OrderItemViewRequest(10L, 2)));
        var inactiveReq = new OrderCreateRequest(1L, 1L, List.of(new OrderItemViewRequest(10L, 2)));
        var missingProductReq = new OrderCreateRequest(1L, 1L, List.of(new OrderItemViewRequest(99L, 1)));

        // Caso válido
        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(orderItemMapper.toEntity(any())).thenReturn(new OrderItem());
        when(orderMapper.toEntity(validReq)).thenReturn(new Order());
        when(orderRepo.saveAndFlush(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);
        when(orderCalc.calculateOrderTotal(any())).thenReturn(new BigDecimal("10.00"));
        when(stockCalc.validateStock(any(), anyInt())).thenReturn(true);
        doNothing().when(stockCalc).reduceStock(any(), anyInt());

        var out = service.createOrder(validReq);
        assertEquals(100L, out.id());
        verify(orderRepo).saveAndFlush(any());

        // Cliente no existe
        when(customerRepo.findById(99L)).thenReturn(Optional.empty());
        var reqNoCustomer = new OrderCreateRequest(99L, 1L, List.of(new OrderItemViewRequest(10L, 1)));
        assertThrows(ResponseStatusException.class, () -> service.createOrder(reqNoCustomer));

        // Producto no existe
        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.createOrder(missingProductReq));

        // Dirección no pertenece al cliente
        var other = new Customer(); other.setId(2L);
        when(customerRepo.findById(2L)).thenReturn(Optional.of(other));
        var wrongAddressReq = new OrderCreateRequest(2L, 1L, List.of(new OrderItemViewRequest(10L, 1)));
        assertThrows(ResponseStatusException.class, () -> service.createOrder(wrongAddressReq));

        // Producto inactivo o stock insuficiente
        product.setActive(false);
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(stockCalc).validateStock(any(), anyInt());
        assertThrows(ResponseStatusException.class, () -> service.createOrder(inactiveReq));
    }

    //Consulta y listado
    @Test
    void listAndGetOrders_shouldWorkOrThrowNotFound() {
        var page = new PageImpl<>(List.of(order), PAGE, 1);
        when(orderRepo.findAll(any(Specification.class), eq(PAGE))).thenReturn(page);
        when(orderMapper.toResponse(order)).thenReturn(response);

        // Filtros combinados
        var result = service.listOrders(Optional.of(1L),
                Optional.of(LocalDate.now().minusDays(1)),
                Optional.of(LocalDate.now()),
                Optional.of(OrderStatus.CREATED),
                PAGE);
        assertEquals(1, result.getTotalElements());

        // Filtros individuales
        service.listOrders(Optional.empty(), Optional.of(LocalDate.now().minusDays(10)),
                Optional.empty(), Optional.empty(), PAGE);
        service.listOrders(Optional.empty(), Optional.empty(),
                Optional.of(LocalDate.now()), Optional.empty(), PAGE);

        // Pedido existente
        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);
        assertEquals(100L, service.getOrderById(100L).id());

        // Pedido inexistente
        when(orderRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.getOrderById(999L));
    }

    //Cambio de estado
    @Test
    void updateOrderStatus_shouldHandleValidInvalidAndMissingOrders() {
        var reqValid = new OrderStatusUpdateRequest("PAID");
        var reqInvalid = new OrderStatusUpdateRequest("SHIPPED");
        var reqBadEnum = new OrderStatusUpdateRequest("INVALID");

        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));
        when(transitionValidator.isValidTransition(OrderStatus.CREATED, OrderStatus.PAID)).thenReturn(true);
        when(orderRepo.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);

        // Caso válido
        assertNotNull(service.updateOrderStatus(100L, reqValid));
        verify(orderRepo).save(order);

        // Transición inválida
        when(transitionValidator.isValidTransition(OrderStatus.CREATED, OrderStatus.SHIPPED)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> service.updateOrderStatus(100L, reqInvalid));

        // Pedido no encontrado
        when(orderRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.updateOrderStatus(999L, reqValid));

        // Estado inválido (no enum)
        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));
        assertThrows(ResponseStatusException.class, () -> service.updateOrderStatus(100L, reqBadEnum));
    }

    //Cálculo de totales
    @Test
    void buildOrderResponse_shouldCalculateLineTotalsCorrectly() {
        var productA = Product.builder().id(10L).price(new BigDecimal("7.50")).build();
        var productB = Product.builder().id(20L).price(new BigDecimal("12.00")).build();

        var item1 = new OrderItem(); item1.setProduct(productA); item1.setQuantity(2); item1.setUnitPrice(productA.getPrice());
        var item2 = new OrderItem(); item2.setProduct(productB); item2.setQuantity(1); item2.setUnitPrice(productB.getPrice());

        var order = new Order();
        order.setId(200L);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);
        order.setItems(List.of(item1, item2));
        order.setTotal(new BigDecimal("27.00"));
        var cust = new Customer(); cust.setId(1L); order.setCustomer(cust);
        var addr = new Address(); addr.setId(1L); order.setShippingAddress(addr);

        when(orderCalc.calculateLineTotal(productA, 2)).thenReturn(new BigDecimal("15.00"));
        when(orderCalc.calculateLineTotal(productB, 1)).thenReturn(new BigDecimal("12.00"));

        var out = service.buildOrderResponse(order);

        assertEquals(new BigDecimal("27.00"), out.total());
        assertEquals(2, out.items().size());
        assertEquals(new BigDecimal("15.00"), out.items().get(0).lineTotal());
        assertEquals(new BigDecimal("12.00"), out.items().get(1).lineTotal());
    }
}
