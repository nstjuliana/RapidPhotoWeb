package com.rapidphotoupload.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for error responses.
 * 
 * This DTO represents standardized error responses returned by the API
 * when exceptions occur. It provides consistent error information including
 * error type, message, timestamp, and request path.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class ErrorResponseDto {
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("path")
    private String path;
    
    public ErrorResponseDto() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ErrorResponseDto(String error, String message, String path) {
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}

