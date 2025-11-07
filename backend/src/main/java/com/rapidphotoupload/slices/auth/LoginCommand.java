package com.rapidphotoupload.slices.auth;

/**
 * Command object for user login.
 * 
 * This command encapsulates the email and password needed for authentication.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class LoginCommand {
    
    private final String email;
    private final String password;
    
    public LoginCommand(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
}

