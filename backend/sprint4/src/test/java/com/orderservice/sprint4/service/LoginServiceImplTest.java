//package com.orderservice.sprint4.service;
//
//import com.orderservice.sprint4.dto.LoginDTO;
//import com.orderservice.sprint4.dto.LoginResponseDTO;
//import com.orderservice.sprint4.exception.ExternalServiceException;
//import com.orderservice.sprint4.exception.InvalidLoginException;
//import com.orderservice.sprint4.service.impl.LoginServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.http.*;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.client.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class LoginServiceImplTest {
//
//    @InjectMocks
//    private LoginServiceImpl loginService;
//
//    @Mock
//    private RestTemplate restTemplate;
//
//    @Captor
//    ArgumentCaptor<LoginDTO> loginCaptor;
//
//    private final String loginUrl = "http://localhost:8090/api/auth/login";
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        // restTemplate is already injected by @Mock + @InjectMocks
//        // Inject value into private field using reflection
//        ReflectionTestUtils.setField(loginService, "LOGIN_SERVICE_LOGIN_VALIDATION_URL", loginUrl);
//    }
//
//
//
//    @Test
//    void testSuccessfulLoginReturnsToken() {
//        LoginDTO dto = new LoginDTO();
//        dto.setEmail("test@example.com");
//        dto.setPassword("password");
//        LoginResponseDTO responseDTO = new LoginResponseDTO();
//        responseDTO.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcGlyeWFsYUBuaXN1bS5jb20iLCJyb2xlcyI6WyJjdXN0b21lciIsIm9tc19hZG1pbiJdLCJpYXQiOjE3NTIxNDYzOTcsImV4cCI6MTg1MjIzMjc5N30.J4sj_79kRNHyzdOLnbGYccVoTiqA17xopAN_B2BQFTk");
//
//        ResponseEntity<LoginResponseDTO> response = ResponseEntity.ok(responseDTO);
//
//        when(restTemplate.postForEntity(eq(loginUrl), eq(dto), eq(LoginResponseDTO.class)))
//                .thenReturn(response);
//
//        String token = loginService.validateLogin(dto);
//        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcGlyeWFsYUBuaXN1bS5jb20iLCJyb2xlcyI6WyJjdXN0b21lciIsIm9tc19hZG1pbiJdLCJpYXQiOjE3NTIxNDYzOTcsImV4cCI6MTg1MjIzMjc5N30.J4sj_79kRNHyzdOLnbGYccVoTiqA17xopAN_B2BQFTk", token);
//    }
//
//    @Test
//    void testLoginReturnsErrorStatus() {
//        LoginDTO dto = new LoginDTO();
//        dto.setEmail("test@example.com");
//        dto.setPassword("password");
//        ResponseEntity<LoginResponseDTO> errorResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//
//        when(restTemplate.postForEntity(eq(loginUrl), eq(dto), eq(LoginResponseDTO.class)))
//                .thenReturn(errorResponse);
//
//        assertThrows(InvalidLoginException.class, () -> loginService.validateLogin(dto));
//    }
//
//    @Test
//    void testLoginReturnsNullBody() {
//        LoginDTO dto = new LoginDTO();
//        dto.setEmail("test@example.com");
//        dto.setPassword("password");
//        ResponseEntity<LoginResponseDTO> response = ResponseEntity.ok(null);
//
//        when(restTemplate.postForEntity(eq(loginUrl), eq(dto), eq(LoginResponseDTO.class)))
//                .thenReturn(response);
//
//        assertThrows(InvalidLoginException.class, () -> loginService.validateLogin(dto));
//    }
//
//    @Test
//    void testLoginReturnsBlankToken() {
//        LoginDTO dto = new LoginDTO();
//        dto.setEmail("test@example.com");
//        dto.setPassword("password");
//        LoginResponseDTO responseDTO = new LoginResponseDTO();
//        responseDTO.setToken("");
//        ResponseEntity<LoginResponseDTO> response = ResponseEntity.ok(responseDTO);
//
//        when(restTemplate.postForEntity(eq(loginUrl), eq(dto), eq(LoginResponseDTO.class)))
//                .thenReturn(response);
//
//        assertThrows(InvalidLoginException.class, () -> loginService.validateLogin(dto));
//    }
//
//    @Test
//    void testHttpClientErrorThrowsInvalidLoginException() {
//        LoginDTO dto = new LoginDTO();
//        dto.setEmail("test@example.com");
//        dto.setPassword("password");
//
//        when(restTemplate.postForEntity(eq(loginUrl), eq(dto), eq(LoginResponseDTO.class)))
//                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
//
//        assertThrows(InvalidLoginException.class, () -> loginService.validateLogin(dto));
//    }
//
//    @Test
//    void testHttpServerErrorThrowsExternalServiceException() {
//        LoginDTO dto = new LoginDTO();
//        dto.setEmail("test@example.com");
//        dto.setPassword("password");
//        when(restTemplate.postForEntity(eq(loginUrl), eq(dto), eq(LoginResponseDTO.class)))
//                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
//
//        assertThrows(ExternalServiceException.class, () -> loginService.validateLogin(dto));
//    }
//
//    @Test
//    void testResourceAccessExceptionThrowsExternalServiceException() {
//        LoginDTO dto = new LoginDTO();
//        dto.setEmail("test@example.com");
//        dto.setPassword("password");
//        when(restTemplate.postForEntity(eq(loginUrl), eq(dto), eq(LoginResponseDTO.class)))
//                .thenThrow(new ResourceAccessException("Timeout"));
//
//        assertThrows(ExternalServiceException.class, () -> loginService.validateLogin(dto));
//    }
//
//    @Test
//    void testUnexpectedExceptionThrowsExternalServiceException() {
//        LoginDTO dto = new LoginDTO();
//        dto.setEmail("test@example.com");
//        dto.setPassword("password");
//
//        when(restTemplate.postForEntity(eq(loginUrl), eq(dto), eq(LoginResponseDTO.class)))
//                .thenThrow(new RuntimeException("Unexpected"));
//
//        assertThrows(ExternalServiceException.class, () -> loginService.validateLogin(dto));
//    }
//}
