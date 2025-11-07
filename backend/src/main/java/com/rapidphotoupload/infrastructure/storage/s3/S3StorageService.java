package com.rapidphotoupload.infrastructure.storage.s3;

import com.rapidphotoupload.infrastructure.storage.StorageAdapter;
import com.rapidphotoupload.shared.exceptions.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

/**
 * S3 implementation of StorageAdapter.
 * 
 * This service provides presigned URL generation for direct client uploads/downloads to S3,
 * eliminating the need for files to pass through the backend server. This improves performance
 * and reduces backend load for high-volume uploads.
 * 
 * Uses AWS SDK v2 S3Presigner for generating presigned URLs with configurable expiration times.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Service
@Primary
public class S3StorageService implements StorageAdapter {
    
    private final S3Client s3Client;
    private final String bucketName;
    private final S3Presigner s3Presigner;
    
    /**
     * Default expiration time for upload URLs (15 minutes).
     */
    private static final long DEFAULT_UPLOAD_EXPIRATION_MINUTES = 15;
    
    /**
     * Default expiration time for download URLs (60 minutes).
     */
    private static final long DEFAULT_DOWNLOAD_EXPIRATION_MINUTES = 60;
    
    public S3StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.s3Presigner = s3Presigner;
    }
    
    @Override
    public Mono<String> generatePresignedUploadUrl(String key, String contentType, long expirationMinutes) {
        if (key == null || key.isBlank()) {
            return Mono.error(new IllegalArgumentException("S3 key cannot be null or blank"));
        }
        if (contentType == null || contentType.isBlank()) {
            return Mono.error(new IllegalArgumentException("Content type cannot be null or blank"));
        }
        if (expirationMinutes < 1 || expirationMinutes > 1440) {
            return Mono.error(new IllegalArgumentException(
                    "Expiration minutes must be between 1 and 1440 (24 hours)"));
        }
        
        return Mono.fromCallable(() -> {
            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .build();
                
                PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(expirationMinutes))
                        .putObjectRequest(putObjectRequest)
                        .build();
                
                PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
                return presignedRequest.url().toString();
            } catch (Exception e) {
                throw new StorageException("Failed to generate presigned upload URL for key: " + key, e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<String> generatePresignedDownloadUrl(String key, long expirationMinutes) {
        if (key == null || key.isBlank()) {
            return Mono.error(new IllegalArgumentException("S3 key cannot be null or blank"));
        }
        if (expirationMinutes < 1 || expirationMinutes > 1440) {
            return Mono.error(new IllegalArgumentException(
                    "Expiration minutes must be between 1 and 1440 (24 hours)"));
        }
        
        return Mono.fromCallable(() -> {
            try {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                
                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(expirationMinutes))
                        .getObjectRequest(getObjectRequest)
                        .build();
                
                PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
                return presignedRequest.url().toString();
            } catch (Exception e) {
                throw new StorageException("Failed to generate presigned download URL for key: " + key, e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<Void> deleteObject(String key) {
        if (key == null || key.isBlank()) {
            return Mono.error(new IllegalArgumentException("S3 key cannot be null or blank"));
        }
        
        return Mono.fromRunnable(() -> {
            try {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                s3Client.deleteObject(deleteRequest);
            } catch (Exception e) {
                throw new StorageException("Failed to delete object with key: " + key, e);
            }
        }).subscribeOn(Schedulers.boundedElastic())
        .then();
    }
}
