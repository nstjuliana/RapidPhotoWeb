package com.rapidphotoupload.slices.auth;

import com.rapidphotoupload.domain.user.User;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.domain.user.UserRepository;
import com.rapidphotoupload.infrastructure.security.jwt.JwtTokenProvider;
import com.rapidphotoupload.shared.exceptions.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command handler for user signup.
 * 
 * This handler processes SignupCommand requests by:
 * 1. Checking if user with email already exists
 * 2. Creating new User domain entity with BCrypt-hashed password
 * 3. Saving user to repository
 * 4. Generating JWT access and refresh tokens
 * 5. Returning tokens and user info
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class SignupCommandHandler {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public SignupCommandHandler(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * Handles the signup command.
     * 
     * @param command The command containing email and password
     * @return Mono containing LoginResponseDto with tokens and user info
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
                            // Hash password using BCrypt
                            String passwordHash = passwordEncoder.encode(command.getPassword());
                            
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
                    // Generate JWT tokens
                    String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
                    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
                    
                    // Create response
                    LoginResponseDto response = new LoginResponseDto();
                    response.setAccessToken(accessToken);
                    response.setRefreshToken(refreshToken);
                    response.setExpiresIn(900L); // 15 minutes in seconds
                    response.setUserId(user.getId().getValue().toString());
                    response.setEmail(user.getEmail());
                    
                    return Mono.just(response);
                });
    }
}

