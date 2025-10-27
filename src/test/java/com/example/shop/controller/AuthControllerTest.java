package com.example.shop.controller;

import com.example.shop.dto.security.in.LoginRequest;
import com.example.shop.dto.security.out.TokenResponse;
import com.example.shop.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de AuthController para cubrir los tres flujos:
 * 1. Credenciales válidas
 * 2. Credenciales inválidas (BadCredentialsException)
 * 3. Error genérico de autenticación (AuthenticationException)
 */
class AuthControllerTest {

    @Test
    void login_whenCredentialsAreValid_returnsTokenResponse() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        JwtTokenUtil jwtTokenUtil = mock(JwtTokenUtil.class);
        AuthController controller = new AuthController(authManager, jwtTokenUtil);

        LoginRequest request = new LoginRequest("admin", "1234");

        Authentication authentication = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(jwtTokenUtil.generateToken("admin")).thenReturn("fake.jwt.token");

        ResponseEntity<TokenResponse> response = controller.login(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        TokenResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("fake.jwt.token", body.getToken());

        // Verifica interacciones
        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenUtil, times(1)).generateToken("admin");
        verifyNoMoreInteractions(jwtTokenUtil);
    }

    @Test
    void login_whenCredentialsAreInvalid_shouldThrowBadCredentialsException() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        JwtTokenUtil jwtTokenUtil = mock(JwtTokenUtil.class);
        AuthController controller = new AuthController(authManager, jwtTokenUtil);

        LoginRequest request = new LoginRequest("admin", "WRONG");
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> controller.login(request));

        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtTokenUtil);
    }

    @Test
    void login_whenGenericAuthError_shouldThrowAuthenticationException() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        JwtTokenUtil jwtTokenUtil = mock(JwtTokenUtil.class);
        AuthController controller = new AuthController(authManager, jwtTokenUtil);

        LoginRequest request = new LoginRequest("admin", "ERROR");
        AuthenticationException ex = mock(AuthenticationException.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(ex);

        assertThrows(AuthenticationException.class, () -> controller.login(request));

        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtTokenUtil);
    }
}
