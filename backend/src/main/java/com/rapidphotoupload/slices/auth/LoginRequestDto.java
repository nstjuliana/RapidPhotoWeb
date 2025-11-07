package com.rapidphotoupload.slices.auth;

/**
 * Request DTO for login endpoint.
 * 
 * This DTO represents the request body for user login.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class LoginRequestDto {
    
    private String email;
    private String password;
    
    public LoginRequestDto() {
    }
    
    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

