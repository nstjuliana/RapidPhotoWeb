package com.rapidphotoupload.integration;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.domain.uploadjob.UploadJobId;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.slices.photo.PhotoController;
import com.rapidphotoupload.slices.photo.PhotoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Integration tests for photo query endpoints.
 * 
 * Tests the complete flow of:
 * - Listing photos with pagination
 * - Filtering photos by tags
 * - Retrieving single photo details
 * - Getting presigned download URLs
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PhotoQueryIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private PhotoRepository photoRepository;
    
    private UserId testUserId;
    private PhotoId photoId1;
    private PhotoId photoId2;
    
    @BeforeEach
    void setUp() {
        // Create test user ID
        testUserId = UserId.of(UUID.randomUUID());
        
        // Create test photos
        photoId1 = PhotoId.of(UUID.randomUUID());
        photoId2 = PhotoId.of(UUID.randomUUID());
        
        // Save test photos
        Photo photo1 = new Photo(
                photoId1,
                testUserId,
                null,
                "test-photo-1.jpg",
                "s3-key-1",
                LocalDateTime.now(),
                Set.of("vacation", "beach"),
                "COMPLETED"
        );
        
        Photo photo2 = new Photo(
                photoId2,
                testUserId,
                null,
                "test-photo-2.jpg",
                "s3-key-2",
                LocalDateTime.now().minusDays(1),
                Set.of("vacation", "mountain"),
                "COMPLETED"
        );
        
        photoRepository.save(photo1).block();
        photoRepository.save(photo2).block();
    }
    
    @Test
    void testListPhotos() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/photos")
                        .queryParam("userId", testUserId.getValue().toString())
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PhotoDto.class)
                .hasSize(2);
    }
    
    @Test
    void testListPhotosWithTagFilter() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/photos")
                        .queryParam("userId", testUserId.getValue().toString())
                        .queryParam("tags", "vacation,beach")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PhotoDto.class)
                .hasSize(1)
                .consumeWith(result -> {
                    PhotoDto photo = result.getResponseBody().get(0);
                    assert photo.getTags().contains("vacation");
                    assert photo.getTags().contains("beach");
                });
    }
    
    @Test
    void testGetPhotoById() {
        webTestClient.get()
                .uri("/api/photos/{photoId}", photoId1.getValue().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhotoDto.class)
                .consumeWith(result -> {
                    PhotoDto photo = result.getResponseBody();
                    assert photo.getId().equals(photoId1.getValue().toString());
                    assert photo.getFilename().equals("test-photo-1.jpg");
                    assert photo.getDownloadUrl() != null;
                });
    }
    
    @Test
    void testGetPhotoByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        
        webTestClient.get()
                .uri("/api/photos/{photoId}", nonExistentId.toString())
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    void testGetDownloadUrl() {
        webTestClient.get()
                .uri("/api/photos/{photoId}/download", photoId1.getValue().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.downloadUrl").exists()
                .jsonPath("$.expirationMinutes").isEqualTo(60);
    }
}

