package com.rapidphotoupload.slices.auth;

import com.rapidphotoupload.domain.user.User;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.domain.user.UserRepository;
import com.rapidphotoupload.infrastructure.security.MockAuthService;
import com.rapidphotoupload.shared.exceptions.ValidationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command handler for user signup.
 * 
 * This handler processes SignupCommand requests by:
 * 1. Checking if user with email already exists
 * 2. Creating new User domain entity with hashed password
 * 3. Saving user to repository
 * 4. Generating authentication token
 * 5. Returning token and user info
 * 
 * Note: Password hashing uses simple SHA-256 for Phase 4 mock auth.
 * This will be replaced with proper password hashing (BCrypt) in Phase 8.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class SignupCommandHandler {
    
    private final UserRepository userRepository;
    private final MockAuthService mockAuthService;
    
    public SignupCommandHandler(
            UserRepository userRepository,
            MockAuthService mockAuthService) {
        this.userRepository = userRepository;
        this.mockAuthService = mockAuthService;
    }
    
    /**
     * Handles the signup command.
     * 
     * @param command The command containing email and password
     * @return Mono containing LoginResponseDto with token and user info
     */
    public Mono<LoginResponseDto> handle(SignupCommand command) {
        // Validate command inputs
        if (command.getEmail() == null || command.getEmail().isBlank()) {
            return Mono.error(new ValidationException("Email cannot be null or blank"));
        }
        if (command.getPassword() == null || command.getPassword().isBlank()) {
            return Mono.error(new ValidationException("Password cannot be null or blank"));
        }
        
        // Check if user already exists
        return userRepository.findByEmail(command.getEmail())
                .flatMap(existingUser -> 
                    Mono.<User>error(new ValidationException("User with email already exists: " + command.getEmail()))
                )
                .switchIfEmpty(
                    // User doesn't exist, create new one
                    Mono.defer(() -> {
                        try {
                            // Hash password
                            String passwordHash = hashPassword(command.getPassword());
                            
                            // Create new user
                            UserId userId = UserId.of(UUID.randomUUID());
                            User newUser = new User(
                                    userId,
                                    command.getEmail(),
                                    passwordHash,
                                    LocalDateTime.now()
                            );
                            
                            // Save user
                            return userRepository.save(newUser);
                        } catch (IllegalArgumentException e) {
                            // Wrap domain validation errors
                            return Mono.error(new ValidationException("Invalid user data: " + e.getMessage()));
                        } catch (Exception e) {
                            // Wrap any other errors
                            return Mono.error(new com.rapidphotoupload.shared.exceptions.DomainException(
                                    "Failed to create user: " + e.getMessage(), e));
                        }
                    })
                )
                .flatMap(user -> {
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

