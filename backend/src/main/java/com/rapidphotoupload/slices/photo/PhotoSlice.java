package com.rapidphotoupload.slices.photo;

/**
 * Photo Query and Command Slice - Architectural Documentation
 * 
 * This slice implements the photo query and command functionality for retrieving,
 * managing, and deleting photos with pagination, filtering, and detail views.
 * 
 * Query Flow:
 * 1. PhotoController receives HTTP GET requests
 * 2. Controller creates Query objects (ListPhotosQuery or GetPhotoQuery)
 * 3. Query handlers (ListPhotosQueryHandler, GetPhotoQueryHandler) process queries
 * 4. Handlers call PhotoRepository to fetch domain entities
 * 5. Handlers convert Photo entities to PhotoDto
 * 6. Handlers generate presigned download URLs using StorageAdapter
 * 7. DTOs returned to controller and serialized to JSON
 * 
 * Command Flow (Delete):
 * 1. PhotoController receives HTTP DELETE request
 * 2. Controller creates DeletePhotoCommand with photoId and userId
 * 3. DeletePhotoCommandHandler processes the command
 * 4. Handler loads photo from PhotoRepository and verifies ownership
 * 5. Handler deletes photo file from S3 storage using StorageAdapter
 * 6. Handler deletes photo record from database using PhotoRepository
 * 7. Returns 204 No Content on success
 * 
 * Components:
 * - PhotoController: REST endpoints for photo queries and commands
 * - ListPhotosQuery: Query object for listing photos
 * - ListPhotosQueryHandler: Handler for list queries with pagination/tag filtering
 * - GetPhotoQuery: Query object for single photo retrieval
 * - GetPhotoQueryHandler: Handler for single photo queries
 * - DeletePhotoCommand: Command object for deleting photos
 * - DeletePhotoCommandHandler: Handler for delete commands
 * - PhotoDto: Data transfer object for photo responses
 * 
 * Dependencies:
 * - PhotoRepository: Domain repository for photo persistence
 * - StorageAdapter: Infrastructure service for S3 presigned URL generation and deletion
 * 
 * Endpoints:
 * - GET /api/photos?userId={userId}&page={page}&size={size}&sortBy={sortBy}&tags={tags}
 * - GET /api/photos/{photoId}
 * - GET /api/photos/{photoId}/download
 * - DELETE /api/photos/{photoId}
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class PhotoSlice {
    // This class serves as architectural documentation only.
    // No implementation code here.
}

