package com.rapidphotoupload.slices.auth;

/**
 * Command object for user signup.
 * 
 * This command encapsulates the email and password needed for user registration.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class SignupCommand {
    
    private final String email;
    private final String password;
    
    public SignupCommand(String email, String password) {
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

