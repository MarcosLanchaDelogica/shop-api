package com.example.shop.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @Test
    void doFilter_addsAndClearsTraceId_andAccessibleViaStaticMethod() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/test");
        var response = new MockHttpServletResponse();

        // Creamos un FilterChain personalizado para verificar el estado dentro del flujo
        FilterChain chain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                // Dentro del filtro, el traceId debe existir
                String insideTrace = TraceIdFilter.getTraceId();
                assertNotNull(insideTrace, "El traceId debería estar disponible dentro del filtro");
                assertEquals(insideTrace, MDC.get("traceId"));
            }
        };

        // Antes de ejecutar el filtro, no debe haber traceId
        assertNull(TraceIdFilter.getTraceId());
        assertNull(MDC.get("traceId"));

        // Ejecutamos el filtro
        filter.doFilter(request, response, chain);

        // Después del filtro, el traceId debe limpiarse correctamente
        assertNull(TraceIdFilter.getTraceId(), "El traceId debería limpiarse tras el filtro");
        assertNull(MDC.get("traceId"), "El MDC debería limpiarse tras el filtro");
    }
}
