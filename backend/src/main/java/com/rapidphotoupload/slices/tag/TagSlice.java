package com.rapidphotoupload.slices.tag;

/**
 * Tag Management Slice - Architectural Documentation
 * 
 * This slice implements tag management functionality for adding, removing,
 * and replacing tags on photos.
 * 
 * Flow:
 * 1. TagController receives HTTP POST/DELETE/PUT requests
 * 2. Controller creates TagPhotoCommand with photoId, tags, and operation
 * 3. TagPhotoCommandHandler processes the command
 * 4. Handler loads photo from PhotoRepository
 * 5. Handler applies tag operation using domain methods (addTag, removeTag)
 * 6. Handler saves updated photo
 * 7. Handler converts Photo to PhotoDto with download URL
 * 8. DTO returned to controller and serialized to JSON
 * 
 * Components:
 * - TagController: REST endpoints for tag operations
 * - TagPhotoCommand: Command object for tag operations
 * - TagPhotoCommandHandler: Handler for tag commands
 * - TagOperation: Enumeration of operation types (ADD, REMOVE, REPLACE)
 * - TagRequestDto: Request DTO for tag operations
 * 
 * Dependencies:
 * - PhotoRepository: Domain repository for photo persistence
 * - StorageAdapter: Infrastructure service for S3 presigned URL generation
 * 
 * Endpoints:
 * - POST /api/photos/{photoId}/tags - Add tags to photo
 * - DELETE /api/photos/{photoId}/tags - Remove tags from photo
 * - PUT /api/photos/{photoId}/tags - Replace all tags on photo
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class TagSlice {
    // This class serves as architectural documentation only.
    // No implementation code here.
}

