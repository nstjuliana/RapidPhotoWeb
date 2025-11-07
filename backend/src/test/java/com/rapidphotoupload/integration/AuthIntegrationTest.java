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
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
                    assert response.getToken() != null;
                    assert response.getEmail().equals(testEmail);
                    assert response.getUserId() != null;
                });
    }
    
    @Test
    void testSignupDuplicateEmail() {
        // Create user first
        UserId userId = UserId.of(UUID.randomUUID());
        String passwordHash = hashPassword(testPassword);
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
        String passwordHash = hashPassword(testPassword);
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
                    assert response.getToken() != null;
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
        String passwordHash = hashPassword(testPassword);
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
        
        String token = loginResponse.getToken();
        
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
        String passwordHash = hashPassword(testPassword);
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
        
        String token = loginResponse.getToken();
        
        // Logout
        AuthController.ValidateTokenRequestDto logoutRequest = 
                new AuthController.ValidateTokenRequestDto(token);
        
        webTestClient.post()
                .uri("/api/auth/logout")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(logoutRequest)
                .exchange()
                .expectStatus().isOk();
        
        // Verify token is invalidated
        AuthController.ValidateTokenRequestDto validateRequest = 
                new AuthController.ValidateTokenRequestDto(token);
        
        webTestClient.post()
                .uri("/api/auth/validate")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validateRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    /**
     * Helper method to hash password (same as in handlers).
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

