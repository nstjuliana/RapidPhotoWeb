package com.rapidphotoupload.slices.auth;

import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.infrastructure.security.MockAuthService;
import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Query handler for token validation.
 * 
 * This handler processes ValidateTokenQuery requests by:
 * 1. Validating token via MockAuthService
 * 2. Returning UserId if token is valid
 * 3. Throwing exception if token is invalid
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class ValidateTokenQueryHandler {
    
    private final MockAuthService mockAuthService;
    
    public ValidateTokenQueryHandler(MockAuthService mockAuthService) {
        this.mockAuthService = mockAuthService;
    }
    
    /**
     * Handles the validate token query.
     * 
     * @param query The query containing token string
     * @return Mono containing UserId if token is valid
     */
    public Mono<UserId> handle(ValidateTokenQuery query) {
        return Mono.fromCallable(() -> mockAuthService.validateToken(query.getToken()))
                .onErrorMap(Exception.class, e -> 
                    new AuthenticationException("Invalid or expired token")
                );
    }
}

