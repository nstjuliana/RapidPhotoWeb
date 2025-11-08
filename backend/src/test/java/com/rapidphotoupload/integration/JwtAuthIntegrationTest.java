package com.rapidphotoupload.integration;

import com.rapidphotoupload.domain.user.User;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.domain.user.UserRepository;
import com.rapidphotoupload.slices.auth.LoginRequestDto;
import com.rapidphotoupload.slices.auth.LoginResponseDto;
import com.rapidphotoupload.slices.auth.RefreshTokenRequestDto;
import com.rapidphotoupload.slices.auth.RefreshTokenResponseDto;
import com.rapidphotoupload.slices.auth.SignupRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Integration tests for JWT authentication endpoints.
 * 
 * Tests the complete flow of:
 * - User signup with JWT tokens
 * - User login with JWT tokens
 * - Token refresh
 * - Token validation
 * - Protected endpoint access
 * - Authorization checks
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JwtAuthIntegrationTest {
    
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
        testEmail = "jwt-test-" + UUID.randomUUID() + "@example.com";
        testPassword = "password123";
    }
    
    @Test
    void testSignupWithJwtTokens() {
        SignupRequestDto request = new SignupRequestDto(testEmail, testPassword);
        
        webTestClient.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .consumeWith(result -> {
                    LoginResponseDto response = result.getResponseBody();
                    assert response != null;
                    assert response.getAccessToken() != null;
                    assert response.getRefreshToken() != null;
                    assert response.getExpiresIn() == 900L; // 15 minutes
                    assert response.getEmail().equals(testEmail);
                    assert response.getUserId() != null;
                    
                    // Verify tokens are JWT format (start with eyJ)
                    assert response.getAccessToken().startsWith("eyJ");
                    assert response.getRefreshToken().startsWith("eyJ");
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
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
    
    @Test
    void testLoginWithJwtTokens() {
        // Create user with BCrypt password
        UserId userId = UserId.of(UUID.randomUUID());
        String passwordHash = passwordEncoder.encode(testPassword);
        User user = new User(userId, testEmail, passwordHash, LocalDateTime.now());
        userRepository.save(user).block();
        
        // Login
        LoginRequestDto request = new LoginRequestDto(testEmail, testPassword);
        
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .consumeWith(result -> {
                    LoginResponseDto response = result.getResponseBody();
                    assert response != null;
                    assert response.getAccessToken() != null;
                    assert response.getRefreshToken() != null;
                    assert response.getExpiresIn() == 900L;
                    assert response.getEmail().equals(testEmail);
                    assert response.getUserId().equals(userId.getValue().toString());
                });
    }
    
    @Test
    void testLoginInvalidCredentials() {
        LoginRequestDto request = new LoginRequestDto("nonexistent@example.com", "wrongpassword");
        
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    void testLoginWrongPassword() {
        // Create user
        UserId userId = UserId.of(UUID.randomUUID());
        String passwordHash = passwordEncoder.encode(testPassword);
        User user = new User(userId, testEmail, passwordHash, LocalDateTime.now());
        userRepository.save(user).block();
        
        // Try login with wrong password
        LoginRequestDto request = new LoginRequestDto(testEmail, "wrongpassword");
        
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    void testRefreshToken() {
        // Login to get tokens
        LoginResponseDto loginResponse = loginAndGetTokens();
        String refreshToken = loginResponse.getRefreshToken();
        
        // Refresh token
        RefreshTokenRequestDto request = new RefreshTokenRequestDto(refreshToken);
        
        webTestClient.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RefreshTokenResponseDto.class)
                .consumeWith(result -> {
                    RefreshTokenResponseDto response = result.getResponseBody();
                    assert response != null;
                    assert response.getAccessToken() != null;
                    assert response.getExpiresIn() == 900L;
                    // New access token should be different from original
                    assert !response.getAccessToken().equals(loginResponse.getAccessToken());
                });
    }
    
    @Test
    void testRefreshTokenWithAccessToken() {
        // Login to get tokens
        LoginResponseDto loginResponse = loginAndGetTokens();
        String accessToken = loginResponse.getAccessToken();
        
        // Try to use access token as refresh token (should fail)
        RefreshTokenRequestDto request = new RefreshTokenRequestDto(accessToken);
        
        webTestClient.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    void testRefreshTokenInvalid() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalid-refresh-token");
        
        webTestClient.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    void testProtectedEndpointWithToken() {
        // Login to get token
        LoginResponseDto loginResponse = loginAndGetTokens();
        String accessToken = loginResponse.getAccessToken();
        
        // Access protected endpoint
        webTestClient.get()
                .uri("/api/photos?page=0&size=20")
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void testProtectedEndpointWithoutToken() {
        webTestClient.get()
                .uri("/api/photos?page=0&size=20")
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    void testProtectedEndpointWithInvalidToken() {
        webTestClient.get()
                .uri("/api/photos?page=0&size=20")
                .header("Authorization", "Bearer invalid-token")
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    void testPublicEndpointsWithoutToken() {
        // Health endpoint should be public
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
        
        // Auth endpoints should be public
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequestDto("test@example.com", "password"))
                .exchange()
                .expectStatus().isUnauthorized(); // But will fail due to invalid credentials
    }
    
    /**
     * Helper method to login and get tokens.
     */
    private LoginResponseDto loginAndGetTokens() {
        // Create user first
        UserId userId = UserId.of(UUID.randomUUID());
        String passwordHash = passwordEncoder.encode(testPassword);
        User user = new User(userId, testEmail, passwordHash, LocalDateTime.now());
        userRepository.save(user).block();
        
        // Login
        LoginRequestDto loginRequest = new LoginRequestDto(testEmail, testPassword);
        
        return webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .returnResult()
                .getResponseBody();
    }
}

