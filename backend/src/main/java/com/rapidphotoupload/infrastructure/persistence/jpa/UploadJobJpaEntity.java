package com.rapidphotoupload.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing an UploadJob in the database.
 * 
 * This entity maps to the 'upload_jobs' table and includes JPA annotations for persistence.
 * It serves as the infrastructure layer representation of the UploadJob domain entity.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Entity
@Table(name = "upload_jobs")
@EntityListeners(AuditingEntityListener.class)
public class UploadJobJpaEntity {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    
    @Column(name = "total_files", nullable = false)
    private Integer totalFiles;
    
    @Column(name = "completed_files", nullable = false)
    private Integer completedFiles;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor for JPA
    public UploadJobJpaEntity() {
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getTotalFiles() {
        return totalFiles;
    }
    
    public void setTotalFiles(Integer totalFiles) {
        this.totalFiles = totalFiles;
    }
    
    public Integer getCompletedFiles() {
        return completedFiles;
    }
    
    public void setCompletedFiles(Integer completedFiles) {
        this.completedFiles = completedFiles;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

