package com.example.shop.testsupport;

import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.domain.model.*;
import com.example.shop.dto.customer.in.CustomerCreateRequest;
import com.example.shop.dto.customer.in.CustomerUpdateRequest;
import com.example.shop.dto.customer.in.AddressCreateRequest;
import com.example.shop.dto.order.in.OrderItemViewRequest;
import com.example.shop.dto.product.in.ProductCreateRequest;
import com.example.shop.dto.product.in.ProductUpdateRequest;
import com.example.shop.dto.order.in.OrderCreateRequest;
import com.example.shop.dto.order.in.OrderStatusUpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class TestData {
    private TestData() {}

    //DOMAIN BUILDERS
    public static Product product(Long id, String sku, String name, BigDecimal price, boolean active) {
        return Product.builder()
                .id(id)
                .sku(sku)
                .name(name)
                .price(price)
                .active(active)
                .build();
    }

    public static Customer customer(Long id, String email) {
        return Customer.builder()
                .id(id)
                .email(email)
                .fullName("Ana López")
                .build();
    }

    public static Address address(Long id, String city) {
        return Address.builder()
                .id(id)
                .line1("C/ Mayor 1")
                .city(city)
                .postalCode("28001")
                .country("ES")
                .isDefault(true)
                .build();
    }

    //DTO BUILDERS
    public static ProductCreateRequest newProductReq(String sku, String name, String description,
                                                     BigDecimal price, Integer stock, Boolean active) {
        return new ProductCreateRequest(sku, name, description, price, stock, active);
    }

    public static ProductUpdateRequest updateProductReq(String name, String description,
                                                        BigDecimal price, Integer stock, Boolean active) {
        return new ProductUpdateRequest(name, description, price, stock, active);
    }

    public static CustomerCreateRequest newCustomerReq(String email) {
        return new CustomerCreateRequest("Ana López", email, "+34600123456");
    }

    public static CustomerUpdateRequest updateCustomerReq(String fullName, String email) {
        return new CustomerUpdateRequest(fullName, email, "+34600987654");
    }

    public static AddressCreateRequest newAddressReq() {
        return new AddressCreateRequest(null, "C/ Mayor 1", null, "Madrid", "28001", "ES", true);
    }

    public static AddressCreateRequest newAddressReqAlt() {
        return new AddressCreateRequest(null, "Av. Libertad 22", "Piso 3", "Barcelona", "08001", "ES", false);
    }

    public static OrderItemViewRequest itemReq(Long productId, int qty) {
        return new OrderItemViewRequest(productId, qty);
    }

    public static OrderCreateRequest newOrderReq(Long customerId, Long addressId, Long productId, int qty) {
        return new OrderCreateRequest(customerId, addressId, List.of(itemReq(productId, qty)));
    }

    public static OrderStatusUpdateRequest updateOrderStatusReq(OrderStatus status) {
        return new OrderStatusUpdateRequest(status.name());
    }

    //FECHAS DE SOPORTE
    public static LocalDate d(String iso) {
        return LocalDate.parse(iso);
    }
}
