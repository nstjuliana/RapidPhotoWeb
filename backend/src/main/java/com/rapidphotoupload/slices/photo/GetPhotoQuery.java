package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.user.UserId;

/**
 * Query object for retrieving a single photo by ID.
 * 
 * This query encapsulates the photo ID and user ID needed to retrieve photo details
 * and verify ownership.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class GetPhotoQuery {
    
    private final PhotoId photoId;
    private final UserId userId;
    
    public GetPhotoQuery(PhotoId photoId, UserId userId) {
        this.photoId = photoId;
        this.userId = userId;
    }
    
    public PhotoId getPhotoId() {
        return photoId;
    }
    
    public UserId getUserId() {
        return userId;
    }
}

