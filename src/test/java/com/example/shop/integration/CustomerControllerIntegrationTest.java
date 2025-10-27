package com.example.shop.integration;

import com.example.shop.testsupport.Json;
import com.example.shop.testsupport.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void get_update_listWithFilter_delete_and_setDefaultAddress() throws Exception {
        // Crear cliente
        var created = mvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.newCustomerReq("ana2@example.com"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long customerId = ((Number) com.jayway.jsonpath.JsonPath
                .read(created.getResponse().getContentAsString(), "$.id")).longValue();

        // GET /api/customers/{id}
        mvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.email").value("ana2@example.com"));

        // PUT /api/customers/{id}
        var updateBody = Json.toJson(TestData.updateCustomerReq("Ana María", "ana.updated@example.com"));
        mvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana.updated@example.com"));

        // Crear dos direcciones
        var addr1 = mvc.perform(post("/api/customers/{id}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.newAddressReq())))
                .andExpect(status().isOk())
                .andReturn();

        Long address1Id = ((Number) com.jayway.jsonpath.JsonPath
                .read(addr1.getResponse().getContentAsString(), "$[0].id")).longValue();

        var addr2 = mvc.perform(post("/api/customers/{id}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.newAddressReqAlt())))
                .andExpect(status().isOk())
                .andReturn();

        Long address2Id = ((Number) com.jayway.jsonpath.JsonPath
                .read(addr2.getResponse().getContentAsString(), "$[1].id")).longValue();

        // PUT /api/customers/{id}/addresses/{addressId}/default
        mvc.perform(put("/api/customers/{id}/addresses/{addressId}/default", customerId, address2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(address2Id))
                .andExpect(jsonPath("$.isDefault").value(true));

        // GET /api/customers?email=ana.updated@example.com&page=0&size=5
        mvc.perform(get("/api/customers")
                        .param("email", "ana.updated@example.com")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("ana.updated@example.com"));

        // DELETE /api/customers/{id}
        mvc.perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isNoContent());
    }
}
