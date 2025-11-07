package com.rapidphotoupload.infrastructure.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Health indicator for AWS S3 connectivity.
 * 
 * Checks if the application can access the configured S3 bucket.
 * Returns UP if bucket is accessible, DOWN otherwise.
 * Will show DOWN if AWS credentials are not configured (expected in Phase 1).
 */
@Component
public class S3HealthIndicator implements HealthIndicator {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:rapid-photo-upload-dev}")
    private String bucketName;

    @Autowired
    public S3HealthIndicator(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public Health health() {
        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.headBucket(request);
            
            return Health.up()
                    .withDetail("service", "AWS S3")
                    .withDetail("bucket", bucketName)
                    .withDetail("status", "Bucket accessible")
                    .build();
        } catch (S3Exception e) {
            if (e.statusCode() == 403 || e.statusCode() == 404) {
                return Health.down()
                        .withDetail("service", "AWS S3")
                        .withDetail("bucket", bucketName)
                        .withDetail("status", "Bucket not accessible")
                        .withDetail("error", e.getMessage())
                        .build();
            }
            return Health.down()
                    .withDetail("service", "AWS S3")
                    .withDetail("bucket", bucketName)
                    .withDetail("status", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        } catch (Exception e) {
            // Handle cases where credentials are not configured
            return Health.down()
                    .withDetail("service", "AWS S3")
                    .withDetail("bucket", bucketName)
                    .withDetail("status", "Not configured")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}





