package com.rapidphotoupload.slices.auth;

/**
 * Query object for token validation.
 * 
 * This query encapsulates the token string needed to validate authentication.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class ValidateTokenQuery {
    
    private final String token;
    
    public ValidateTokenQuery(String token) {
        this.token = token;
    }
    
    public String getToken() {
        return token;
    }
}

