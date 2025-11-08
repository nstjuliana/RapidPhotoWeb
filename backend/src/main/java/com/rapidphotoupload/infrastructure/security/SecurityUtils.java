package com.rapidphotoupload.infrastructure.security;

import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Utility class for extracting authentication information from Spring Security context.
 * 
 * Provides reactive methods for accessing current user information:
 * - getCurrentUserId() - extracts UserId from security context
 * 
 * All methods return Mono for reactive composition.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class SecurityUtils {
    
    /**
     * Extracts the current user ID from the security context.
     * 
     * The user ID is stored in the Authentication principal as a string.
     * 
     * @return Mono containing UserId of the authenticated user
     * @throws AuthenticationException if user is not authenticated
     */
    public static Mono<UserId> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .switchIfEmpty(Mono.error(new AuthenticationException("User is not authenticated")))
                .map(Authentication::getPrincipal)
                .cast(String.class)
                .map(userIdString -> {
                    try {
                        return UserId.of(UUID.fromString(userIdString));
                    } catch (IllegalArgumentException e) {
                        throw new AuthenticationException("Invalid user ID format in security context");
                    }
                });
    }
    
    /**
     * Extracts the current user's email from the security context.
     * 
     * The email is stored in the Authentication details.
     * 
     * @return Mono containing email address of the authenticated user
     * @throws AuthenticationException if user is not authenticated or email not found
     */
    public static Mono<String> getCurrentUserEmail() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .switchIfEmpty(Mono.error(new AuthenticationException("User is not authenticated")))
                .map(Authentication::getDetails)
                .cast(String.class)
                .switchIfEmpty(Mono.error(new AuthenticationException("User email not found in security context")));
    }
}

