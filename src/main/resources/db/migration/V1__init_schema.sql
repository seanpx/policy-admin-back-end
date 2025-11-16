-- Flyway V1 baseline: migrate former schema.sql
-- Creates product configuration table

CREATE TABLE IF NOT EXISTS prod_config_t5688 (
    prod_code CHAR(3),
    prod_desc CHAR(50),
    prod_category CHAR(10)
);

