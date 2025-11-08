package com.rapidphotoupload.slices.auth;

import com.rapidphotoupload.domain.user.User;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.domain.user.UserRepository;
import com.rapidphotoupload.infrastructure.security.jwt.JwtTokenProvider;
import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Command handler for user login.
 * 
 * This handler processes LoginCommand requests by:
 * 1. Finding user by email in repository
 * 2. Validating password using BCrypt
 * 3. Generating JWT access and refresh tokens
 * 4. Returning tokens and user info
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class LoginCommandHandler {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public LoginCommandHandler(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * Handles the login command.
     * 
     * @param command The command containing email and password
     * @return Mono containing LoginResponseDto with tokens and user info
     */
    public Mono<LoginResponseDto> handle(LoginCommand command) {
        return userRepository.findByEmail(command.getEmail())
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid email or password")))
                .flatMap(user -> {
                    // Validate password using BCrypt
                    if (!passwordEncoder.matches(command.getPassword(), user.getPasswordHash())) {
                        return Mono.error(new AuthenticationException("Invalid email or password"));
                    }
                    
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

