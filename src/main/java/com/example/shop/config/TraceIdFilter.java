package com.example.shop.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class TraceIdFilter implements Filter {

    // Añadimos un ThreadLocal para poder recuperar el traceId desde otros componentes
    private static final ThreadLocal<String> CURRENT_TRACE_ID = new ThreadLocal<>();

    // Método estático accesible desde cualquier parte (SecurityConfig, handlers, etc.)
    public static String getTraceId() {
        return CURRENT_TRACE_ID.get();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String traceId = UUID.randomUUID().toString();
        CURRENT_TRACE_ID.set(traceId);      // guardamos el trace en el hilo
        MDC.put("traceId", traceId);        // también lo añadimos al MDC para logs

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        log.info("→ [{}] {} {}", traceId, httpRequest.getMethod(), httpRequest.getRequestURI());

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
            CURRENT_TRACE_ID.remove(); // limpiamos el contexto al terminar
        }
    }
}
