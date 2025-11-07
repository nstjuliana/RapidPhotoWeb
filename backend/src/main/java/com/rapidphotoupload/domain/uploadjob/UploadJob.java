package com.rapidphotoupload.domain.uploadjob;

import com.rapidphotoupload.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * UploadJob domain entity representing a batch upload operation.
 * 
 * This entity tracks the progress of a batch upload containing multiple photos.
 * It maintains:
 * - Total number of files in the batch
 * - Number of completed files
 * - Current upload status
 * - Business logic for progress tracking and status transitions
 * 
 * This is a pure domain entity with no infrastructure dependencies (no JPA annotations).
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class UploadJob {
    
    private final UploadJobId id;
    private final UserId userId;
    private UploadStatus status;
    private final int totalFiles;
    private int completedFiles;
    private final LocalDateTime createdAt;
    
    /**
     * Creates a new UploadJob entity.
     * 
     * @param id The unique identifier for this upload job
     * @param userId The user who initiated this upload job
     * @param totalFiles The total number of files in this upload batch
     * @param createdAt The date and time when the upload job was created
     * @throws IllegalArgumentException if totalFiles is less than 1
     */
    public UploadJob(UploadJobId id, UserId userId, int totalFiles, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "UploadJob ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        
        if (totalFiles < 1) {
            throw new IllegalArgumentException("Total files must be at least 1");
        }
        this.totalFiles = totalFiles;
        this.completedFiles = 0;
        this.status = UploadStatus.PENDING;
    }
    
    /**
     * Creates a new UploadJob entity with initial state.
     * 
     * @param id The unique identifier for this upload job
     * @param userId The user who initiated this upload job
     * @param status The initial status of the upload job
     * @param totalFiles The total number of files in this upload batch
     * @param completedFiles The number of files that have been completed
     * @param createdAt The date and time when the upload job was created
     * @throws IllegalArgumentException if totalFiles is less than 1 or completedFiles is invalid
     */
    public UploadJob(
            UploadJobId id,
            UserId userId,
            UploadStatus status,
            int totalFiles,
            int completedFiles,
            LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "UploadJob ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        
        if (totalFiles < 1) {
            throw new IllegalArgumentException("Total files must be at least 1");
        }
        this.totalFiles = totalFiles;
        
        if (completedFiles < 0 || completedFiles > totalFiles) {
            throw new IllegalArgumentException(
                    "Completed files must be between 0 and total files (" + totalFiles + ")");
        }
        this.completedFiles = completedFiles;
    }
    
    /**
     * Increments the count of completed files.
     * 
     * If all files are completed, automatically marks the job as COMPLETED.
     * 
     * @throws IllegalStateException if the job is already completed or failed
     */
    public void incrementCompleted() {
        if (status == UploadStatus.COMPLETED) {
            throw new IllegalStateException("Cannot increment completed files: job is already completed");
        }
        if (status == UploadStatus.FAILED) {
            throw new IllegalStateException("Cannot increment completed files: job has failed");
        }
        
        if (completedFiles >= totalFiles) {
            throw new IllegalStateException("Cannot increment: all files already completed");
        }
        
        completedFiles++;
        
        // Auto-transition to COMPLETED when all files are done
        if (completedFiles == totalFiles) {
            markAsCompleted();
        } else if (status == UploadStatus.PENDING) {
            // Start uploading when first file completes
            status = UploadStatus.UPLOADING;
        }
    }
    
    /**
     * Marks this upload job as completed.
     * 
     * @throws IllegalStateException if not all files are completed
     */
    public void markAsCompleted() {
        if (completedFiles != totalFiles) {
            throw new IllegalStateException(
                    "Cannot mark as completed: " + completedFiles + " of " + totalFiles + " files completed");
        }
        this.status = UploadStatus.COMPLETED;
    }
    
    /**
     * Marks this upload job as failed.
     */
    public void markAsFailed() {
        this.status = UploadStatus.FAILED;
    }
    
    /**
     * Calculates the progress percentage of this upload job.
     * 
     * @return Progress as a percentage (0.0 to 100.0)
     */
    public double getProgress() {
        if (totalFiles == 0) {
            return 0.0;
        }
        return (completedFiles * 100.0) / totalFiles;
    }
    
    /**
     * Checks if this upload job is completed.
     * 
     * @return true if status is COMPLETED, false otherwise
     */
    public boolean isCompleted() {
        return status == UploadStatus.COMPLETED;
    }
    
    /**
     * Checks if this upload job has failed.
     * 
     * @return true if status is FAILED, false otherwise
     */
    public boolean isFailed() {
        return status == UploadStatus.FAILED;
    }
    
    /**
     * Checks if this upload job is still in progress.
     * 
     * @return true if status is PENDING or UPLOADING, false otherwise
     */
    public boolean isInProgress() {
        return status.isInProgress();
    }
    
    // Getters
    
    public UploadJobId getId() {
        return id;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public UploadStatus getStatus() {
        return status;
    }
    
    public int getTotalFiles() {
        return totalFiles;
    }
    
    public int getCompletedFiles() {
        return completedFiles;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadJob uploadJob = (UploadJob) o;
        return Objects.equals(id, uploadJob.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "UploadJob{" +
                "id=" + id +
                ", userId=" + userId +
                ", status=" + status +
                ", totalFiles=" + totalFiles +
                ", completedFiles=" + completedFiles +
                ", progress=" + String.format("%.1f%%", getProgress()) +
                '}';
    }
}

