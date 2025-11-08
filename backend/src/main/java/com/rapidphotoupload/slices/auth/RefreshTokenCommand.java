package com.rapidphotoupload.slices.auth;

/**
 * Command for refreshing access tokens.
 * 
 * Contains the refresh token string that will be validated
 * to generate a new access token.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class RefreshTokenCommand {
    
    private final String refreshToken;
    
    public RefreshTokenCommand(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
}

