package com.rapidphotoupload.slices.tag;

import java.util.Set;

/**
 * Request DTO for tag operations.
 * 
 * This DTO represents the request body for adding or removing tags from photos.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class TagRequestDto {
    
    private Set<String> tags;
    
    public TagRequestDto() {
    }
    
    public TagRequestDto(Set<String> tags) {
        this.tags = tags;
    }
    
    public Set<String> getTags() {
        return tags;
    }
    
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}

