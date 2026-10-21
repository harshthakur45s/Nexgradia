-- V1__initial_foundation.sql
-- Nexgradia Backend Foundation: Placeholder foundation migration
-- Creates metadata table to record system foundation initialization.

CREATE TABLE IF NOT EXISTS system_metadata (
    id SERIAL PRIMARY KEY,
    key VARCHAR(100) NOT NULL UNIQUE,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO system_metadata (key, value)
VALUES ('foundation_version', '1.0.0')
ON CONFLICT (key) DO NOTHING;
