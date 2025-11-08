package com.rapidphotoupload.infrastructure.security.jwt;

import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * WebFlux filter for JWT authentication.
 * 
 * This filter:
 * - Extracts JWT token from Authorization header
 * - Validates token using JwtTokenProvider
 * - Creates Authentication object and sets in SecurityContext
 * - Continues filter chain on success
 * - Returns 401 Unauthorized on invalid/missing token
 * 
 * Public endpoints (configured in SecurityConfig) bypass this filter.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {
    
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = getTokenFromRequest(exchange);
        
        if (token == null) {
            // No token present, continue filter chain
            // SecurityConfig will handle authentication requirement
            return chain.filter(exchange);
        }
        
        try {
            // Validate token and extract user info
            // Only access tokens should be used for protected endpoints
            if (jwtTokenProvider.isRefreshToken(token)) {
                // Refresh tokens cannot be used for authentication
                return handleAuthenticationError(exchange, "Refresh tokens cannot be used for authentication");
            }
            
            String userId = jwtTokenProvider.getUserIdFromToken(token).getValue().toString();
            String email;
            try {
                email = jwtTokenProvider.getEmailFromToken(token);
            } catch (AuthenticationException e) {
                // Token doesn't have email (shouldn't happen for access tokens, but handle gracefully)
                email = null;
            }
            
            // Create authentication object
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            // Create UsernamePasswordAuthenticationToken with details (email) if available
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities
            );
            
            // Store email in authentication details for later use (if available)
            if (email != null) {
                authentication.setDetails(email);
            }
            
            // Set authentication in security context
            SecurityContext securityContext = new SecurityContextImpl(authentication);
            
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
                    
        } catch (AuthenticationException e) {
            // Invalid token - return 401 Unauthorized
            return handleAuthenticationError(exchange, e.getMessage());
        } catch (Exception e) {
            // Unexpected error - log and return 401 Unauthorized
            // In production, log the error for debugging
            return handleAuthenticationError(exchange, "Authentication failed");
        }
    }
    
    /**
     * Extracts JWT token from Authorization header.
     * 
     * @param exchange The server web exchange
     * @return Token string if present, null otherwise
     */
    private String getTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }
    
    /**
     * Handles authentication errors by returning 401 Unauthorized response.
     * 
     * @param exchange The server web exchange
     * @param message Error message
     * @return Mono completing the response
     */
    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("WWW-Authenticate", "Bearer");
        return response.setComplete();
    }
}

