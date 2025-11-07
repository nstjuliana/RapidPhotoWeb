package com.rapidphotoupload.domain.uploadjob;

/**
 * Enumeration representing the status of an upload job.
 * 
 * This enum defines the possible states that an UploadJob can be in during its lifecycle:
 * - PENDING: Job created but upload not yet started
 * - UPLOADING: Upload is currently in progress
 * - COMPLETED: All files in the job have been successfully uploaded
 * - FAILED: The upload job encountered an error and cannot proceed
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public enum UploadStatus {
    
    /**
     * Upload job has been created but upload has not yet started.
     */
    PENDING,
    
    /**
     * Upload is currently in progress.
     */
    UPLOADING,
    
    /**
     * All files in the upload job have been successfully uploaded.
     */
    COMPLETED,
    
    /**
     * The upload job encountered an error and cannot proceed.
     */
    FAILED;
    
    /**
     * Checks if the status indicates the upload is still in progress.
     * 
     * @return true if status is PENDING or UPLOADING, false otherwise
     */
    public boolean isInProgress() {
        return this == PENDING || this == UPLOADING;
    }
    
    /**
     * Checks if the status indicates the upload is complete (successfully or failed).
     * 
     * @return true if status is COMPLETED or FAILED, false otherwise
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}

