package com.rapidphotoupload.infrastructure.storage.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuration for AWS S3 client.
 * 
 * Creates and configures the S3Client bean used for interacting with AWS S3.
 * Credentials and region are loaded from environment variables or application properties.
 */
@Configuration
public class S3Config {

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    /**
     * Creates and configures the S3Client bean.
     * 
     * @return Configured S3Client instance
     */
    @Bean
    public S3Client s3Client() {
        if (accessKey == null || accessKey.isEmpty() || 
            secretKey == null || secretKey.isEmpty()) {
            // Return a client that will fail on actual operations if credentials are not set
            // This allows the application to start but health checks will show DOWN status
            return S3Client.builder()
                    .region(Region.of(region))
                    .build();
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}





