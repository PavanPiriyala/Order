package com.orderservice.sprint4.controller;

import com.orderservice.sprint4.dto.LoginDTO;
import com.orderservice.sprint4.exception.ExternalServiceException;
import com.orderservice.sprint4.exception.InvalidLoginException;
import com.orderservice.sprint4.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    @Mock
    private LoginService loginService;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void success_login_returnsToken() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@test.com");
        dto.setPassword("123");

        when(loginService.validateLogin(dto)).thenReturn("token123");

        ResponseEntity<?> response = loginController.validateAndGenerateLogin(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token123", response.getBody());
        verify(loginService).validateLogin(dto);
    }

    @Test
    void nullDto_returnsBadRequest() {
        ResponseEntity<?> response = loginController.validateAndGenerateLogin(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("must not be null"));
        verifyNoInteractions(loginService);
    }

    @Test
    void nullEmailPassword_returnsBadRequest() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail(null);
        dto.setPassword(null);

        ResponseEntity<?> response = loginController.validateAndGenerateLogin(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("must not be null"));
        verifyNoInteractions(loginService);
    }

    @Test
    void invalidLoginException_returnsUnauthorized() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@test.com");
        dto.setPassword("123");

        when(loginService.validateLogin(dto)).thenThrow(new InvalidLoginException("bad credentials"));

        ResponseEntity<?> response = loginController.validateAndGenerateLogin(dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("bad credentials", response.getBody());
        verify(loginService).validateLogin(dto);
    }

    @Test
    void externalServiceException_returnsBadGateway() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@test.com");
        dto.setPassword("123");

        when(loginService.validateLogin(dto)).thenThrow(new ExternalServiceException("service down"));

        ResponseEntity<?> response = loginController.validateAndGenerateLogin(dto);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("service down", response.getBody());
        verify(loginService).validateLogin(dto);
    }

    @Test
    void genericException_returnsServerError() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@test.com");
        dto.setPassword("123");

        when(loginService.validateLogin(dto)).thenThrow(new RuntimeException("surprise"));

        ResponseEntity<?> response = loginController.validateAndGenerateLogin(dto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unexpected error: surprise"));
        verify(loginService).validateLogin(dto);
    }
}
