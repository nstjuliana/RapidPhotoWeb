-- Flyway migration: Insert test user for testing
-- Version: V2
-- Description: Inserts a test user for API testing
-- Password: password123 (BCrypt hash with strength 10)

-- Insert test user
INSERT INTO users (id, email, password_hash, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000'::UUID,
    'test@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

