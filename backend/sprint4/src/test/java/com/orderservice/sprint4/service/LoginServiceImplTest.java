
package com.orderservice.sprint4.service;

import com.orderservice.sprint4.dto.LoginDTO;
import com.orderservice.sprint4.dto.LoginResponseDTO;
import com.orderservice.sprint4.exception.ExternalServiceException;
import com.orderservice.sprint4.exception.InvalidLoginException;
import com.orderservice.sprint4.security.JwtUtil;
import com.orderservice.sprint4.service.impl.LoginServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoginServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private LoginServiceImpl loginService;

    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginDTO = new LoginDTO();
    }

    @Test
    void testValidateLogin_Success_AdminRole() {
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setToken("valid-token");
        ResponseEntity<LoginResponseDTO> responseEntity =
                new ResponseEntity<>(responseDTO, HttpStatus.OK);

        when(restTemplate.postForEntity(any(String.class),
                any(LoginDTO.class),
                eq(LoginResponseDTO.class)))
                .thenReturn(responseEntity);
        when(jwtUtil.validateAdminRole("valid-token")).thenReturn(true);

        String token = loginService.validateLogin(loginDTO);

        assertEquals("valid-token", token);
        verify(restTemplate).postForEntity(any(String.class),
                any(LoginDTO.class),
                eq(LoginResponseDTO.class));
        verify(jwtUtil).validateAdminRole("valid-token");
    }

    @Test
    void testValidateLogin_ResponseErrorStatus() {
        ResponseEntity<LoginResponseDTO> responseEntity =
                new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        when(restTemplate.postForEntity(any(String.class),
                any(LoginDTO.class),
                eq(LoginResponseDTO.class)))
                .thenReturn(responseEntity);

        InvalidLoginException ex = assertThrows(InvalidLoginException.class,
                () -> loginService.validateLogin(loginDTO));
        assertTrue(ex.getMessage().contains("Login service returned error"));
    }

    @Test
    void testValidateLogin_NullOrBlankToken() {
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setToken("");
        ResponseEntity<LoginResponseDTO> responseEntity =
                new ResponseEntity<>(responseDTO, HttpStatus.OK);

        when(restTemplate.postForEntity(any(String.class),
                any(LoginDTO.class),
                eq(LoginResponseDTO.class)))
                .thenReturn(responseEntity);

        InvalidLoginException ex = assertThrows(InvalidLoginException.class,
                () -> loginService.validateLogin(loginDTO));
        assertTrue(ex.getMessage().contains("Login failed: token not found"));
    }

    @Test
    void testValidateLogin_JwtUtilNotAdmin() {
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setToken("valid-token");
        ResponseEntity<LoginResponseDTO> responseEntity =
                new ResponseEntity<>(responseDTO, HttpStatus.OK);

        when(restTemplate.postForEntity(any(String.class),
                any(LoginDTO.class),
                eq(LoginResponseDTO.class)))
                .thenReturn(responseEntity);
        when(jwtUtil.validateAdminRole("valid-token")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> loginService.validateLogin(loginDTO));
        assertEquals("Something went wrong with Login Service", ex.getMessage());
    }

    @Test
    void testValidateLogin_HttpClientError() {
        when(restTemplate.postForEntity(any(String.class),
                any(LoginDTO.class),
                eq(LoginResponseDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        InvalidLoginException ex = assertThrows(InvalidLoginException.class,
                () -> loginService.validateLogin(loginDTO));
        assertTrue(ex.getMessage().contains("Invalid credentials or bad request"));
    }

    @Test
    void testValidateLogin_HttpServerError() {
        when(restTemplate.postForEntity(any(String.class),
                any(LoginDTO.class),
                eq(LoginResponseDTO.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        ExternalServiceException ex = assertThrows(ExternalServiceException.class,
                () -> loginService.validateLogin(loginDTO));
        assertTrue(ex.getMessage().contains("Login service failed"));
    }

    @Test
    void testValidateLogin_ResourceAccess() {
        when(restTemplate.postForEntity(any(String.class),
                any(LoginDTO.class),
                eq(LoginResponseDTO.class)))
                .thenThrow(new ResourceAccessException("I/O error"));

        ExternalServiceException ex = assertThrows(ExternalServiceException.class,
                () -> loginService.validateLogin(loginDTO));
        assertTrue(ex.getMessage().contains("Unable to reach login service"));
    }

    @Test
    void testValidateLogin_UnexpectedException() {
        when(restTemplate.postForEntity(any(String.class),
                any(LoginDTO.class),
                eq(LoginResponseDTO.class)))
                .thenThrow(new RuntimeException("Unexpected"));

        ExternalServiceException ex = assertThrows(ExternalServiceException.class,
                () -> loginService.validateLogin(loginDTO));
        assertTrue(ex.getMessage().contains("Unexpected error during login"));
    }
}
