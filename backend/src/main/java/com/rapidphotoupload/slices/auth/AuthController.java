package com.rapidphotoupload.slices.auth;

import com.rapidphotoupload.domain.user.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for authentication endpoints.
 * 
 * Provides endpoints for:
 * - User login (returns JWT access and refresh tokens)
 * - User signup (creates user, returns JWT tokens)
 * - Token refresh (generates new access token from refresh token)
 * - Token validation (validates JWT token, returns userId)
 * - Logout (client-side token deletion)
 * 
 * All endpoints use reactive types (Mono) for non-blocking operations.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final LoginCommandHandler loginCommandHandler;
    private final SignupCommandHandler signupCommandHandler;
    private final RefreshTokenCommandHandler refreshTokenCommandHandler;
    private final ValidateTokenQueryHandler validateTokenQueryHandler;
    
    public AuthController(
            LoginCommandHandler loginCommandHandler,
            SignupCommandHandler signupCommandHandler,
            RefreshTokenCommandHandler refreshTokenCommandHandler,
            ValidateTokenQueryHandler validateTokenQueryHandler) {
        this.loginCommandHandler = loginCommandHandler;
        this.signupCommandHandler = signupCommandHandler;
        this.refreshTokenCommandHandler = refreshTokenCommandHandler;
        this.validateTokenQueryHandler = validateTokenQueryHandler;
    }
    
    /**
     * User login endpoint.
     * 
     * Request body:
     * { "email": "user@example.com", "password": "password123" }
     * 
     * @param request The login request DTO
     * @return Mono containing LoginResponseDto with access/refresh tokens and user info
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponseDto>> login(@RequestBody LoginRequestDto request) {
        LoginCommand command = new LoginCommand(request.getEmail(), request.getPassword());
        
        return loginCommandHandler.handle(command)
                .map(ResponseEntity::ok)
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.AuthenticationException.class,
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    /**
     * User signup endpoint.
     * 
     * Request body:
     * { "email": "user@example.com", "password": "password123" }
     * 
     * @param request The signup request DTO
     * @return Mono containing LoginResponseDto with access/refresh tokens and user info
     */
    @PostMapping("/signup")
    public Mono<ResponseEntity<LoginResponseDto>> signup(@RequestBody SignupRequestDto request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        SignupCommand command = new SignupCommand(request.getEmail(), request.getPassword());
        
        return signupCommandHandler.handle(command)
                .map(ResponseEntity::ok)
                .onErrorResume(com.rapidphotoupload.shared.exceptions.ValidationException.class, ex -> {
                    // User already exists or validation failed
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                })
                .onErrorResume(com.rapidphotoupload.shared.exceptions.DomainException.class, ex -> {
                    // Domain validation failed (e.g., invalid email format)
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                })
                .onErrorResume(Exception.class, ex -> {
                    // Any other error
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
    
    /**
     * Refresh token endpoint.
     * 
     * Request body:
     * { "refreshToken": "jwt-refresh-token-string" }
     * 
     * @param request The refresh token request DTO
     * @return Mono containing RefreshTokenResponseDto with new access token
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<RefreshTokenResponseDto>> refreshToken(@RequestBody RefreshTokenRequestDto request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        RefreshTokenCommand command = new RefreshTokenCommand(request.getRefreshToken());
        
        return refreshTokenCommandHandler.handle(command)
                .map(ResponseEntity::ok)
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.AuthenticationException.class,
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    /**
     * Token validation endpoint.
     * 
     * Request body:
     * { "token": "jwt-token-string" }
     * 
     * @param request The token validation request
     * @return Mono containing ValidateTokenResponseDto with userId
     */
    @PostMapping("/validate")
    public Mono<ResponseEntity<ValidateTokenResponseDto>> validateToken(@RequestBody ValidateTokenRequestDto request) {
        ValidateTokenQuery query = new ValidateTokenQuery(request.getToken());
        
        return validateTokenQueryHandler.handle(query)
                .map(userId -> {
                    ValidateTokenResponseDto response = new ValidateTokenResponseDto();
                    response.setUserId(userId.getValue().toString());
                    response.setValid(true);
                    return ResponseEntity.ok(response);
                })
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.AuthenticationException.class,
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                new ValidateTokenResponseDto(false, null)
                        ));
    }
    
    /**
     * User logout endpoint.
     * 
     * Note: With stateless JWT tokens, logout is handled client-side by deleting tokens.
     * This endpoint returns success to maintain API compatibility.
     * 
     * @return Mono containing success response
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout() {
        // Client-side logout - tokens are deleted on client
        // No server-side action needed for stateless JWT tokens
        return Mono.just(ResponseEntity.ok().build());
    }
    
    /**
     * Request DTO for token validation.
     */
    public static class ValidateTokenRequestDto {
        private String token;
        
        public ValidateTokenRequestDto() {
        }
        
        public ValidateTokenRequestDto(String token) {
            this.token = token;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
    }
    
    /**
     * Response DTO for token validation.
     */
    public static class ValidateTokenResponseDto {
        private boolean valid;
        private String userId;
        
        public ValidateTokenResponseDto() {
        }
        
        public ValidateTokenResponseDto(boolean valid, String userId) {
            this.valid = valid;
            this.userId = userId;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}

