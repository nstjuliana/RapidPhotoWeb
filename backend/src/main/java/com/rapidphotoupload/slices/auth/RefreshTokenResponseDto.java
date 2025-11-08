package com.rapidphotoupload.slices.auth;

/**
 * Response DTO for refresh token endpoint.
 * 
 * Contains the new access token and expiration time.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class RefreshTokenResponseDto {
    
    private String accessToken;
    private Long expiresIn; // seconds until access token expires
    
    public RefreshTokenResponseDto() {
    }
    
    public RefreshTokenResponseDto(String accessToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}

