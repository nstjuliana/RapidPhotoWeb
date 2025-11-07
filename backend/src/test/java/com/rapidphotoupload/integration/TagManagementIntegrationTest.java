package com.rapidphotoupload.integration;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.slices.tag.TagController;
import com.rapidphotoupload.slices.tag.TagRequestDto;
import com.rapidphotoupload.slices.photo.PhotoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Integration tests for tag management endpoints.
 * 
 * Tests the complete flow of:
 * - Adding tags to photos
 * - Removing tags from photos
 * - Replacing tags on photos
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TagManagementIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private PhotoRepository photoRepository;
    
    private UserId testUserId;
    private PhotoId photoId;
    
    @BeforeEach
    void setUp() {
        // Create test user ID
        testUserId = UserId.of(UUID.randomUUID());
        
        // Create test photo
        photoId = PhotoId.of(UUID.randomUUID());
        
        Photo photo = new Photo(
                photoId,
                testUserId,
                null,
                "test-photo.jpg",
                "s3-key",
                LocalDateTime.now(),
                Set.of("initial-tag"),
                "COMPLETED"
        );
        
        photoRepository.save(photo).block();
    }
    
    @Test
    void testAddTags() {
        TagRequestDto request = new TagRequestDto(Set.of("new-tag-1", "new-tag-2"));
        
        webTestClient.post()
                .uri("/api/photos/{photoId}/tags", photoId.getValue().toString())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhotoDto.class)
                .consumeWith(result -> {
                    PhotoDto photo = result.getResponseBody();
                    assert photo.getTags().contains("new-tag-1");
                    assert photo.getTags().contains("new-tag-2");
                    assert photo.getTags().contains("initial-tag"); // Original tag preserved
                });
    }
    
    @Test
    void testRemoveTags() {
        TagRequestDto request = new TagRequestDto(Set.of("initial-tag"));
        
        webTestClient.method(HttpMethod.DELETE)
                .uri("/api/photos/{photoId}/tags", photoId.getValue().toString())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhotoDto.class)
                .consumeWith(result -> {
                    PhotoDto photo = result.getResponseBody();
                    assert !photo.getTags().contains("initial-tag");
                });
    }
    
    @Test
    void testReplaceTags() {
        TagRequestDto request = new TagRequestDto(Set.of("replaced-tag-1", "replaced-tag-2"));
        
        webTestClient.put()
                .uri("/api/photos/{photoId}/tags", photoId.getValue().toString())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhotoDto.class)
                .consumeWith(result -> {
                    PhotoDto photo = result.getResponseBody();
                    assert photo.getTags().contains("replaced-tag-1");
                    assert photo.getTags().contains("replaced-tag-2");
                    assert !photo.getTags().contains("initial-tag"); // Original tag removed
                });
    }
    
    @Test
    void testTagPhotoNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        TagRequestDto request = new TagRequestDto(Set.of("tag"));
        
        webTestClient.post()
                .uri("/api/photos/{photoId}/tags", nonExistentId.toString())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }
}

