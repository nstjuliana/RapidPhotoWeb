package com.rapidphotoupload.slices.auth;

/**
 * Request DTO for refresh token endpoint.
 * 
 * Contains the refresh token string to be validated.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class RefreshTokenRequestDto {
    
    private String refreshToken;
    
    public RefreshTokenRequestDto() {
    }
    
    public RefreshTokenRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

