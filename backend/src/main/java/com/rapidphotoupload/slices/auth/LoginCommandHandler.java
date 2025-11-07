package com.rapidphotoupload.slices.auth;

import com.rapidphotoupload.domain.user.User;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.domain.user.UserRepository;
import com.rapidphotoupload.infrastructure.security.MockAuthService;
import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Command handler for user login.
 * 
 * This handler processes LoginCommand requests by:
 * 1. Finding user by email in repository
 * 2. Validating password (simple hash comparison for Phase 4)
 * 3. Generating authentication token via MockAuthService
 * 4. Returning token and user info
 * 
 * Note: Password validation uses simple SHA-256 hashing for Phase 4 mock auth.
 * This will be replaced with proper password hashing (BCrypt) in Phase 8.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class LoginCommandHandler {
    
    private final UserRepository userRepository;
    private final MockAuthService mockAuthService;
    
    public LoginCommandHandler(
            UserRepository userRepository,
            MockAuthService mockAuthService) {
        this.userRepository = userRepository;
        this.mockAuthService = mockAuthService;
    }
    
    /**
     * Handles the login command.
     * 
     * @param command The command containing email and password
     * @return Mono containing LoginResponseDto with token and user info
     */
    public Mono<LoginResponseDto> handle(LoginCommand command) {
        return userRepository.findByEmail(command.getEmail())
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid email or password")))
                .flatMap(user -> {
                    // Validate password (simple hash comparison for Phase 4)
                    String passwordHash = hashPassword(command.getPassword());
                    if (!user.getPasswordHash().equals(passwordHash)) {
                        return Mono.error(new AuthenticationException("Invalid email or password"));
                    }
                    
                    // Generate token
                    String token = mockAuthService.generateToken(user.getId());
                    
                    // Create response
                    LoginResponseDto response = new LoginResponseDto();
                    response.setToken(token);
                    response.setUserId(user.getId().getValue().toString());
                    response.setEmail(user.getEmail());
                    
                    return Mono.just(response);
                });
    }
    
    /**
     * Hashes a password using SHA-256 (simple hashing for Phase 4 mock auth).
     * 
     * @param password The plain text password
     * @return The hashed password string
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

