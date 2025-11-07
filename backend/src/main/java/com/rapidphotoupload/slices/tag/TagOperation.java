package com.rapidphotoupload.slices.tag;

/**
 * Enumeration of tag operations that can be performed on photos.
 * 
 * - ADD: Add tags to a photo (existing tags are preserved)
 * - REMOVE: Remove tags from a photo
 * - REPLACE: Replace all tags on a photo with new tags
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public enum TagOperation {
    ADD,
    REMOVE,
    REPLACE
}

