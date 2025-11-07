package com.rapidphotoupload.slices.photo;

/**
 * Photo Query Slice - Architectural Documentation
 * 
 * This slice implements the photo query functionality for retrieving photos
 * with pagination, filtering, and detail views.
 * 
 * Flow:
 * 1. PhotoController receives HTTP GET requests
 * 2. Controller creates Query objects (ListPhotosQuery or GetPhotoQuery)
 * 3. Query handlers (ListPhotosQueryHandler, GetPhotoQueryHandler) process queries
 * 4. Handlers call PhotoRepository to fetch domain entities
 * 5. Handlers convert Photo entities to PhotoDto
 * 6. Handlers generate presigned download URLs using StorageAdapter
 * 7. DTOs returned to controller and serialized to JSON
 * 
 * Components:
 * - PhotoController: REST endpoints for photo queries
 * - ListPhotosQuery: Query object for listing photos
 * - ListPhotosQueryHandler: Handler for list queries with pagination/tag filtering
 * - GetPhotoQuery: Query object for single photo retrieval
 * - GetPhotoQueryHandler: Handler for single photo queries
 * - PhotoDto: Data transfer object for photo responses
 * 
 * Dependencies:
 * - PhotoRepository: Domain repository for photo persistence
 * - StorageAdapter: Infrastructure service for S3 presigned URL generation
 * 
 * Endpoints:
 * - GET /api/photos?userId={userId}&page={page}&size={size}&sortBy={sortBy}&tags={tags}
 * - GET /api/photos/{photoId}
 * - GET /api/photos/{photoId}/download
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class PhotoSlice {
    // This class serves as architectural documentation only.
    // No implementation code here.
}

