package com.rapidphotoupload.slices.upload;

import com.rapidphotoupload.domain.photo.PhotoId;

/**
 * Query object for retrieving upload status.
 * 
 * This query encapsulates the parameters needed to query the status
 * of a photo upload operation. It follows the CQRS query pattern.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class GetUploadStatusQuery {
    
    private final PhotoId photoId;
    
    /**
     * Creates a new GetUploadStatusQuery.
     * 
     * @param photoId The photo ID to query status for
     */
    public GetUploadStatusQuery(PhotoId photoId) {
        this.photoId = photoId;
    }
    
    public PhotoId getPhotoId() {
        return photoId;
    }
}

