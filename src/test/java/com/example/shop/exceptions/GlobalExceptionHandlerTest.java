package com.example.shop.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
        MDC.put("traceId", "trace-xyz");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void handleNotFound_shouldReturn404() {
        var ex = new NotFoundException("No existe");
        var res = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        var body = res.getBody();
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.name(), body.code());
        assertTrue(body.message().contains("No existe"));
        assertEquals("trace-xyz", body.traceId());
    }

    @Test
    void handleConflict_shouldReturn409() {
        var ex = new ConflictException("Duplicado");
        var res = handler.handleConflict(ex, request);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertEquals(ErrorCode.CONFLICT.name(), res.getBody().code());
    }

    @Test
    void handleBadRequest_shouldReturn400() {
        var ex = new BadRequestException("Petición inválida");
        var res = handler.handleBadRequest(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(ErrorCode.BAD_REQUEST.name(), res.getBody().code());
    }

    @Test
    void handleUnauthorized_shouldReturn401() {
        var ex = new UnauthorizedException("Token inválido");
        var res = handler.handleUnauthorized(ex, request);
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertEquals(ErrorCode.UNAUTHORIZED.name(), res.getBody().code());
    }

    @Test
    void handleForbidden_shouldReturn403() {
        var ex = new AccessDeniedException("no permitido");
        var res = handler.handleForbidden(ex, request);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertEquals(ErrorCode.FORBIDDEN.name(), res.getBody().code());
        assertTrue(res.getBody().message().contains("Acceso denegado"));
    }

    @Test
    void handleBadCredentials_shouldReturnUnauthorized() {
        // Arrange
        var ex = new org.springframework.security.authentication.BadCredentialsException("Credenciales erróneas");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        MDC.put("traceId", "trace-001");

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        ResponseEntity<ApiError> response = handler.handleBadCredentials(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ApiError body = response.getBody();
        assertNotNull(body);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), body.status());
        assertEquals("Unauthorized", body.error());
        assertEquals(ErrorCode.BAD_CREDENTIALS.name(), body.code());
        assertEquals("Usuario o contraseña incorrectos", body.message());
        assertEquals("/api/auth/login", body.path());
        assertEquals("trace-001", body.traceId());
        assertNotNull(body.timestamp());
    }


    @Test
    void handleDataIntegrityViolation_shouldReturn409() {
        var ex = new DataIntegrityViolationException("FK error");
        var res = handler.handleDataIntegrityViolation(ex, request);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertEquals(ErrorCode.DATA_INTEGRITY_VIOLATION.name(), res.getBody().code());
        assertTrue(res.getBody().message().contains("asociado"));
    }

    @Test
    void handleValidation_shouldReturn400AndDetails() {
        var binding = mock(org.springframework.validation.BindingResult.class);
        var fe = new FieldError("obj", "nombre", "no puede ser nulo");
        when(binding.getFieldErrors()).thenReturn(List.of(fe));
        var ex = new MethodArgumentNotValidException(null, binding);

        var res = handler.handleValidation(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        var body = res.getBody();
        assertEquals(ErrorCode.VALIDATION_ERROR.name(), body.code());
        assertEquals(1, body.details().size());
        assertEquals("nombre", body.details().get(0).field());
    }

    @Test
    void handleTypeMismatch_shouldReturn400() {
        var ex = new MethodArgumentTypeMismatchException("abc", Integer.class, "id", null, new IllegalArgumentException());
        var res = handler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        var body = res.getBody();
        assertEquals(ErrorCode.BAD_REQUEST.name(), body.code());
        assertTrue(body.message().contains("id"));
        assertEquals(1, body.details().size());
    }

    @Test
    void handleMalformedJson_shouldReturn400() {
        var ex = new HttpMessageNotReadableException("mal JSON");
        var res = handler.handleMalformedJson(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(ErrorCode.BAD_REQUEST.name(), res.getBody().code());
    }

    @Test
    void handleResponseStatus_shouldUseStatusAndReason() {
        var ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        var res = handler.handleResponseStatus(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        var body = res.getBody();
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.name(), body.code());
        assertTrue(body.message().contains("Producto no encontrado"));
    }

    @Test
    void handleResponseStatus_withoutReason_shouldFallbackMessage() {
        var ex = new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var res = handler.handleResponseStatus(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertTrue(res.getBody().message().contains("Bad Request"));
    }

    @Test
    void handleNoSuchElement_shouldReturn404() {
        var ex = new NoSuchElementException("Nada");
        var res = handler.handleNoSuchElement(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.name(), res.getBody().code());
    }

    @Test
    void handleGeneric_shouldReturn500() {
        var ex = new RuntimeException("Boom interno");
        var res = handler.handleGeneric(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        var body = res.getBody();
        assertEquals(ErrorCode.INTERNAL_ERROR.name(), body.code());
        assertTrue(body.message().contains("Boom interno"));
    }

    @Test
    void buildError_shouldIncludeTraceIdAndPath() {
        var res = handler.handleNotFound(new NotFoundException("x"), request);
        var body = res.getBody();
        assertEquals("/api/test", body.path());
        assertEquals("trace-xyz", body.traceId());
        assertNotNull(body.timestamp());
    }

    @Test
    void handleResponseStatus_shouldHandleDefaultCase() {
        // status no contemplado en el switch -> default INTERNAL_ERROR
        var ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error no previsto");
        var res = handler.handleResponseStatus(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        ApiError body = res.getBody();
        assertNotNull(body);
        assertEquals(ErrorCode.INTERNAL_ERROR.name(), body.code());
        assertTrue(body.message().contains("Error no previsto"));
        assertEquals("/api/test", body.path());
    }

    @Test
    void buildError_whenTraceIdIsNull_shouldStillReturnValidError() {
        MDC.clear(); // deja traceId nulo para cubrir esa rama
        var res = handler.handleGeneric(new Exception("Algo falló"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        ApiError body = res.getBody();
        assertNotNull(body);
        assertEquals(ErrorCode.INTERNAL_ERROR.name(), body.code());
        assertNull(body.traceId());
        assertTrue(body.message().contains("Algo falló"));
    }

    @Test
    void handleResponseStatus_shouldMapConflictCode() {
        var ex = new ResponseStatusException(HttpStatus.CONFLICT, "Conflicto detectado");
        var res = handler.handleResponseStatus(ex, request);

        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        ApiError body = res.getBody();
        assertNotNull(body);
        assertEquals(ErrorCode.CONFLICT.name(), body.code());  // <- cubre "case CONFLICT -> ..."
        assertTrue(body.message().contains("Conflicto detectado"));
        assertEquals("/api/test", body.path());
    }

    @Test
    void handleResponseStatus_whenStatusCodeNotHttpStatus_shouldFallbackTo500() {
        // 499 no existe en HttpStatus, así que HttpStatusCode.valueOf(499) NO es instancia de HttpStatus
        var weirdCode = HttpStatusCode.valueOf(499);
        var ex = new ResponseStatusException(weirdCode, "Estado no estándar");
        var res = handler.handleResponseStatus(ex, request);

        // Al no ser HttpStatus, cae en el lado derecho del ternario -> INTERNAL_SERVER_ERROR
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        ApiError body = res.getBody();
        assertNotNull(body);
        assertEquals(ErrorCode.INTERNAL_ERROR.name(), body.code()); // <- cubre "default" del switch y la línea "String code = switch (status) {"
        assertTrue(body.message().contains("Estado no estándar"));
        assertEquals("/api/test", body.path());
    }


}
