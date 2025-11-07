package com.rapidphotoupload.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for upload failure requests.
 * 
 * This DTO represents the request body for reporting an upload failure.
 * It includes an error message explaining why the upload failed.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class UploadFailureDto {
    
    @NotBlank(message = "Error message is required")
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    public UploadFailureDto() {
    }
    
    public UploadFailureDto(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

