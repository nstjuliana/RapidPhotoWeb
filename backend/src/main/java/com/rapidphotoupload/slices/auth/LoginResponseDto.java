package com.rapidphotoupload.slices.auth;

/**
 * Response DTO for login and signup endpoints.
 * 
 * This DTO represents the response body containing authentication token
 * and user information.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class LoginResponseDto {
    
    private String token;
    private String userId;
    private String email;
    
    public LoginResponseDto() {
    }
    
    public LoginResponseDto(String token, String userId, String email) {
        this.token = token;
        this.userId = userId;
        this.email = email;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}

