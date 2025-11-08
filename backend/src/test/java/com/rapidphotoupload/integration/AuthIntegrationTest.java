package com.rapidphotoupload.integration;

import com.rapidphotoupload.domain.user.User;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.domain.user.UserRepository;
import com.rapidphotoupload.slices.auth.AuthController;
import com.rapidphotoupload.slices.auth.LoginRequestDto;
import com.rapidphotoupload.slices.auth.LoginResponseDto;
import com.rapidphotoupload.slices.auth.SignupRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Integration tests for authentication endpoints.
 * 
 * Tests the complete flow of:
 * - User signup
 * - User login
 * - Token validation
 * - Logout
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private String testEmail;
    private String testPassword;
    
    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testPassword = "password123";
    }
    
    @Test
    void testSignup() {
        SignupRequestDto request = new SignupRequestDto(testEmail, testPassword);
        
        webTestClient.post()
                .uri("/api/auth/signup")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .consumeWith(result -> {
                    LoginResponseDto response = result.getResponseBody();
                    assert response.getAccessToken() != null;
                    assert response.getEmail().equals(testEmail);
                    assert response.getUserId() != null;
                });
    }
    
    @Test
    void testSignupDuplicateEmail() {
        // Create user first
        UserId userId = UserId.of(UUID.randomUUID());
        String passwordHash = passwordEncoder.encode(testPassword);
        User user = new User(userId, testEmail, passwordHash, LocalDateTime.now());
        userRepository.save(user).block();
        
        // Try to signup with same email
        SignupRequestDto request = new SignupRequestDto(testEmail, testPassword);
        
        webTestClient.post()
                .uri("/api/auth/signup")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
    
    @Test
    void testLogin() {
        // Create user first
        UserId userId = UserId.of(UUID.randomUUID());
        String passwordHash = passwordEncoder.encode(testPassword);
        User user = new User(userId, testEmail, passwordHash, LocalDateTime.now());
        userRepository.save(user).block();
        
        // Login
        LoginRequestDto request = new LoginRequestDto(testEmail, testPassword);
        
        webTestClient.post()
                .uri("/api/auth/login")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .consumeWith(result -> {
                    LoginResponseDto response = result.getResponseBody();
                    assert response.getAccessToken() != null;
                    assert response.getEmail().equals(testEmail);
                    assert response.getUserId().equals(userId.getValue().toString());
                });
    }
    
    @Test
    void testLoginInvalidCredentials() {
        LoginRequestDto request = new LoginRequestDto("nonexistent@example.com", "wrongpassword");
        
        webTestClient.post()
                .uri("/api/auth/login")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    void testValidateToken() {
        // Create user and login to get token
        UserId userId = UserId.of(UUID.randomUUID());
        String passwordHash = passwordEncoder.encode(testPassword);
        User user = new User(userId, testEmail, passwordHash, LocalDateTime.now());
        userRepository.save(user).block();
        
        LoginRequestDto loginRequest = new LoginRequestDto(testEmail, testPassword);
        LoginResponseDto loginResponse = webTestClient.post()
                .uri("/api/auth/login")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .returnResult()
                .getResponseBody();
        
        String token = loginResponse.getAccessToken();
        
        // Validate token
        AuthController.ValidateTokenRequestDto validateRequest = 
                new AuthController.ValidateTokenRequestDto(token);
        
        webTestClient.post()
                .uri("/api/auth/validate")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthController.ValidateTokenResponseDto.class)
                .consumeWith(result -> {
                    AuthController.ValidateTokenResponseDto response = result.getResponseBody();
                    assert response.isValid();
                    assert response.getUserId().equals(userId.getValue().toString());
                });
    }
    
    @Test
    void testValidateTokenInvalid() {
        AuthController.ValidateTokenRequestDto request = 
                new AuthController.ValidateTokenRequestDto("invalid-token");
        
        webTestClient.post()
                .uri("/api/auth/validate")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    void testLogout() {
        // Create user and login to get token
        UserId userId = UserId.of(UUID.randomUUID());
        String passwordHash = passwordEncoder.encode(testPassword);
        User user = new User(userId, testEmail, passwordHash, LocalDateTime.now());
        userRepository.save(user).block();
        
        LoginRequestDto loginRequest = new LoginRequestDto(testEmail, testPassword);
        LoginResponseDto loginResponse = webTestClient.post()
                .uri("/api/auth/login")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .returnResult()
                .getResponseBody();
        
        String token = loginResponse.getAccessToken();
        
        // Logout (stateless JWT - logout is just a no-op, token remains valid until expiration)
        AuthController.ValidateTokenRequestDto logoutRequest = 
                new AuthController.ValidateTokenRequestDto(token);
        
        webTestClient.post()
                .uri("/api/auth/logout")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(logoutRequest)
                .exchange()
                .expectStatus().isOk();
        
        // Note: With stateless JWT, logout doesn't invalidate the token.
        // The token remains valid until expiration. This test verifies the endpoint works,
        // but token validation will still succeed until the token expires.
        // In a production system with token blacklisting, you would verify the token is invalidated here.
    }
}

