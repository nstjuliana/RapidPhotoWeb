package com.rapidphotoupload.slices.auth;

import com.rapidphotoupload.domain.user.User;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.domain.user.UserRepository;
import com.rapidphotoupload.infrastructure.security.jwt.JwtTokenProvider;
import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Command handler for refreshing access tokens.
 * 
 * This handler:
 * 1. Validates the refresh token
 * 2. Extracts user ID from refresh token
 * 3. Loads user from repository
 * 4. Generates new access token
 * 5. Returns new access token (and optionally new refresh token)
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class RefreshTokenCommandHandler {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    public RefreshTokenCommandHandler(
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }
    
    /**
     * Handles the refresh token command.
     * 
     * @param command The command containing refresh token
     * @return Mono containing RefreshTokenResponseDto with new access token
     */
    public Mono<RefreshTokenResponseDto> handle(RefreshTokenCommand command) {
        // Validate refresh token
        if (command.getRefreshToken() == null || command.getRefreshToken().isBlank()) {
            return Mono.error(new AuthenticationException("Refresh token is required"));
        }
        
        // Check if token is a refresh token
        if (!jwtTokenProvider.isRefreshToken(command.getRefreshToken())) {
            return Mono.error(new AuthenticationException("Invalid refresh token"));
        }
        
        // Extract user ID from refresh token
        UserId userId = jwtTokenProvider.getUserIdFromToken(command.getRefreshToken());
        
        // Load user to get email for new access token
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new AuthenticationException("User not found")))
                .map(user -> {
                    // Generate new access token
                    String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
                    
                    // Create response
                    RefreshTokenResponseDto response = new RefreshTokenResponseDto();
                    response.setAccessToken(newAccessToken);
                    response.setExpiresIn(900L); // 15 minutes in seconds
                    
                    return response;
                });
    }
}

