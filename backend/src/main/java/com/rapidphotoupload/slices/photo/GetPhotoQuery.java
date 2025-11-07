package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.photo.PhotoId;

/**
 * Query object for retrieving a single photo by ID.
 * 
 * This query encapsulates the photo ID needed to retrieve photo details.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class GetPhotoQuery {
    
    private final PhotoId photoId;
    
    public GetPhotoQuery(PhotoId photoId) {
        this.photoId = photoId;
    }
    
    public PhotoId getPhotoId() {
        return photoId;
    }
}

