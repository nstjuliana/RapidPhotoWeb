package com.rapidphotoupload.slices.auth;

/**
 * Response DTO for login and signup endpoints.
 * 
 * This DTO represents the response body containing JWT tokens
 * (access token and refresh token) and user information.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class LoginResponseDto {
    
    private String accessToken;
    private String refreshToken;
    private Long expiresIn; // seconds until access token expires
    private String userId;
    private String email;
    
    public LoginResponseDto() {
    }
    
    public LoginResponseDto(String accessToken, String refreshToken, Long expiresIn, String userId, String email) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
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

