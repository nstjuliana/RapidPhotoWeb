package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.user.UserId;

/**
 * Command for deleting a photo.
 * 
 * Contains the photo ID to delete and the user ID of the authenticated user
 * to verify ownership before deletion.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class DeletePhotoCommand {
    
    private final PhotoId photoId;
    private final UserId userId;
    
    public DeletePhotoCommand(PhotoId photoId, UserId userId) {
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

