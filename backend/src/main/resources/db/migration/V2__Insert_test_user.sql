-- Flyway migration: Insert test user for Phase 3 testing
-- Version: V2
-- Description: Inserts a test user for API testing (Phase 3 uses mock authentication)

-- Insert test user (password hash is a placeholder - not used in Phase 3)
INSERT INTO users (id, email, password_hash, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000'::UUID,
    'test@example.com',
    'dummy_password_hash_for_testing_only_not_used_in_phase_3',
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

