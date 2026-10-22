-- V4__create_resumes_table.sql
-- Nexgradia Secure Resume Management Foundation

CREATE TABLE IF NOT EXISTS resumes (
    id BIGSERIAL PRIMARY KEY,
    student_profile_id BIGINT NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    version_number INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_resumes_profile_version UNIQUE (student_profile_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_resumes_student_profile_id ON resumes(student_profile_id);
CREATE INDEX IF NOT EXISTS idx_resumes_profile_is_active ON resumes(student_profile_id, is_active);
