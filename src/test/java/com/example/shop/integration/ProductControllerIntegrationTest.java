package com.example.shop.integration;

import com.example.shop.testsupport.Json;
import com.example.shop.testsupport.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getById_update_delete_and_listWithFilters() throws Exception {
        // Crear producto inicial
        var created = mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.newProductReq(
                                "SKU-99", "Camiseta", "Camiseta básica", BigDecimal.valueOf(9.99), 50, true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        Long id = ((Number) com.jayway.jsonpath.JsonPath
                .read(created.getResponse().getContentAsString(), "$.id")).longValue();

        // GET /api/products/{id}
        mvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.sku").value("SKU-99"));

        // PUT /api/products/{id}
        var updateBody = Json.toJson(TestData.updateProductReq(
                "Camiseta Premium", "Algodón 100%", BigDecimal.valueOf(14.99), 40, true));
        mvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Camiseta Premium"))
                .andExpect(jsonPath("$.price").value(14.99));

        // Crear otro producto para probar filtros y paginación
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Json.toJson(TestData.newProductReq(
                                "SKU-100", "Camisa lino", "Camisa de lino", BigDecimal.valueOf(29.90), 15, true))))
                .andExpect(status().isCreated());

        // GET /api/products?name=cam&active=true&page=0&size=1&sort=name,asc
        mvc.perform(get("/api/products")
                        .param("name", "cam")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(1));

        // DELETE /api/products/{id}
        mvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        // Verificación: el listado sigue funcionando
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
