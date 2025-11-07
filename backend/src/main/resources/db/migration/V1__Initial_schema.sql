-- Flyway migration: Initial database schema
-- Version: V1
-- Description: Creates initial tables for users, upload_jobs, photos, and photo_tags

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE upload_jobs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    total_files INTEGER NOT NULL,
    completed_files INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE photos (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    upload_job_id UUID REFERENCES upload_jobs(id),
    filename VARCHAR(500) NOT NULL,
    s3_key VARCHAR(1000) NOT NULL,
    upload_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE photo_tags (
    photo_id UUID NOT NULL REFERENCES photos(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (photo_id, tag)
);

-- Indexes for performance optimization
CREATE INDEX idx_photos_user_id ON photos(user_id);
CREATE INDEX idx_photos_upload_job_id ON photos(upload_job_id);
CREATE INDEX idx_upload_jobs_user_id ON upload_jobs(user_id);
CREATE INDEX idx_photo_tags_tag ON photo_tags(tag);

