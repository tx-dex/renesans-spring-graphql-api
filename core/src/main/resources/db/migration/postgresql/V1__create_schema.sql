-- Flyway treats V1 version as baseline, so it schema already exists it skips that step
create schema if not exists data;