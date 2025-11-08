package com.rapidphotoupload.slices.tag;

import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.user.UserId;

import java.util.Set;

/**
 * Command object for tagging or untagging a photo.
 * 
 * This command encapsulates the photo ID, user ID (for authorization),
 * tags to apply, and the operation type (ADD, REMOVE, or REPLACE).
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class TagPhotoCommand {
    
    private final PhotoId photoId;
    private final UserId userId;
    private final Set<String> tags;
    private final TagOperation operation;
    
    public TagPhotoCommand(PhotoId photoId, UserId userId, Set<String> tags, TagOperation operation) {
        this.photoId = photoId;
        this.userId = userId;
        this.tags = tags;
        this.operation = operation;
    }
    
    public PhotoId getPhotoId() {
        return photoId;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public Set<String> getTags() {
        return tags;
    }
    
    public TagOperation getOperation() {
        return operation;
    }
}

