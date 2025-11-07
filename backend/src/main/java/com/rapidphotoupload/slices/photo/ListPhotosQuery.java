package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.user.UserId;

import java.util.Set;

/**
 * Query object for listing photos with pagination and optional tag filtering.
 * 
 * This query encapsulates the parameters needed to retrieve a paginated list
 * of photos for a user, with optional filtering by tags.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class ListPhotosQuery {
    
    private final UserId userId;
    private final int page;
    private final int size;
    private final String sortBy;
    private final Set<String> tags;
    
    public ListPhotosQuery(
            UserId userId,
            int page,
            int size,
            String sortBy,
            Set<String> tags) {
        this.userId = userId;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.tags = tags;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getSize() {
        return size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public Set<String> getTags() {
        return tags;
    }
}

