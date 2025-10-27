package com.example.shop.integration;

import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.testsupport.Json;
import com.example.shop.testsupport.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getOrderById_and_listWithAllFilters() throws Exception {
        // Crear producto
        var p = mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.newProductReq(
                                "SKU-55","Ratón","Ratón óptico", BigDecimal.valueOf(15.50), 30, true))))
                .andExpect(status().isCreated()).andReturn();
        Long productId = ((Number) com.jayway.jsonpath.JsonPath
                .read(p.getResponse().getContentAsString(), "$.id")).longValue();

        // Crear cliente y dirección
        var c = mvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.newCustomerReq("pedro@example.com"))))
                .andExpect(status().isCreated()).andReturn();
        Long customerId = ((Number) com.jayway.jsonpath.JsonPath
                .read(c.getResponse().getContentAsString(), "$.id")).longValue();

        var a = mvc.perform(post("/api/customers/{id}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.newAddressReq())))
                .andExpect(status().isOk()).andReturn();
        Long addressId = ((Number) com.jayway.jsonpath.JsonPath
                .read(a.getResponse().getContentAsString(), "$[0].id")).longValue();

        // Crear pedido
        var orderBody = Json.toJson(TestData.newOrderReq(customerId, addressId, productId, 1));
        var created = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(orderBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn();
        Long orderId = ((Number) com.jayway.jsonpath.JsonPath
                .read(created.getResponse().getContentAsString(), "$.id")).longValue();

        // PUT estado -> PAID para poder filtrar por estado
        mvc.perform(put("/api/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.updateOrderStatusReq(OrderStatus.PAID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        // GET /api/orders/{id}
        mvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));

        // GET /api/orders con todos los filtros y paginación
        var today = LocalDate.now();
        mvc.perform(get("/api/orders")
                        .param("customerId", String.valueOf(customerId))
                        .param("fromDate", today.minusDays(1).toString())
                        .param("toDate", today.plusDays(1).toString())
                        .param("status", "PAID")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "orderDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(orderId));

        //Verificar stock inicial
        var beforeGet = mvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andReturn();
        int stockBefore = ((Number) com.jayway.jsonpath.JsonPath
                .read(beforeGet.getResponse().getContentAsString(), "$.stock")).intValue();

        //Crear nuevo pedido que reducirá stock
        var orderBody2 = Json.toJson(TestData.newOrderReq(customerId, addressId, productId, 2));
        var created2 = mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn();
        Long orderId2 = ((Number) com.jayway.jsonpath.JsonPath
                .read(created2.getResponse().getContentAsString(), "$.id")).longValue();

        //Verificar que el stock se redujo
        var afterCreate = mvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andReturn();
        int stockAfterCreate = ((Number) com.jayway.jsonpath.JsonPath
                .read(afterCreate.getResponse().getContentAsString(), "$.stock")).intValue();

        org.junit.jupiter.api.Assertions.assertEquals(stockBefore - 2, stockAfterCreate,
                "El stock debe reducirse correctamente tras crear el pedido");

        //Cancelar el pedido y verificar que el stock se devuelve
        mvc.perform(put("/api/orders/{id}/status", orderId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.updateOrderStatusReq(OrderStatus.CANCELLED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        //Verificar que el stock se restauró
        var afterCancel = mvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andReturn();
        int stockAfterCancel = ((Number) com.jayway.jsonpath.JsonPath
                .read(afterCancel.getResponse().getContentAsString(), "$.stock")).intValue();

        org.junit.jupiter.api.Assertions.assertEquals(stockBefore, stockAfterCancel,
                "El stock debe restaurarse completamente tras cancelar el pedido");

    }
}
