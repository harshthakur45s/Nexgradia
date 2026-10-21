-- V3__create_student_profiles_and_academics.sql
-- Nexgradia Student Profile and Academic Records

CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    phone_number VARCHAR(20),
    institution VARCHAR(255) NOT NULL,
    program VARCHAR(100) NOT NULL,
    branch VARCHAR(100) NOT NULL,
    graduation_year INT NOT NULL,
    current_semester INT NOT NULL,
    profile_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_student_profiles_user_id ON student_profiles(user_id);

CREATE TABLE IF NOT EXISTS academic_records (
    id BIGSERIAL PRIMARY KEY,
    student_profile_id BIGINT NOT NULL UNIQUE REFERENCES student_profiles(id) ON DELETE CASCADE,
    tenth_board VARCHAR(100) NOT NULL,
    tenth_school VARCHAR(255) NOT NULL,
    tenth_passing_year INT NOT NULL,
    tenth_result_type VARCHAR(20) NOT NULL,
    tenth_score NUMERIC(5, 2) NOT NULL,
    twelfth_board VARCHAR(100) NOT NULL,
    twelfth_school VARCHAR(255) NOT NULL,
    twelfth_passing_year INT NOT NULL,
    twelfth_result_type VARCHAR(20) NOT NULL,
    twelfth_score NUMERIC(5, 2) NOT NULL,
    cumulative_cgpa NUMERIC(5, 2) NOT NULL,
    active_backlogs INT NOT NULL DEFAULT 0,
    cleared_backlogs INT NOT NULL DEFAULT 0,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_academic_records_profile_id ON academic_records(student_profile_id);

CREATE TABLE IF NOT EXISTS semester_records (
    id BIGSERIAL PRIMARY KEY,
    academic_record_id BIGINT NOT NULL REFERENCES academic_records(id) ON DELETE CASCADE,
    semester_number INT NOT NULL,
    result_type VARCHAR(20) NOT NULL,
    score NUMERIC(5, 2) NOT NULL,
    backlogs_count INT NOT NULL DEFAULT 0,
    credits INT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_semester_records_academic_semester UNIQUE (academic_record_id, semester_number)
);

CREATE INDEX IF NOT EXISTS idx_semester_records_academic_record_id ON semester_records(academic_record_id);
