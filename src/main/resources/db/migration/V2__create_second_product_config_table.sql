-- Flyway migration: create second product configuration table (moved from V1)
-- Adjust table/columns as needed for your domain

-- ===============================================
-- Flyway V2: alter product config + add benefits
-- ===============================================

-- 1️⃣ rename and Update existing table

ALTER TABLE prod_config_t5688 RENAME TO prod_config_plan; 

ALTER TABLE prod_config_plan
  ADD COLUMN effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
  ADD COLUMN effective_to   DATE NULL,
  ADD COLUMN is_single_prem BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN is_annual      BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN is_half_yearly BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN is_quarterly   BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN is_monthly     BOOLEAN NOT NULL DEFAULT FALSE;

-- 2️⃣ Define a composite primary key and constraints
ALTER TABLE prod_config_plan
  ADD CONSTRAINT pk_prod_config PRIMARY KEY (prod_code, effective_from);

ALTER TABLE prod_config_plan
  ADD CONSTRAINT ck_effective_dates
  CHECK (effective_to IS NULL OR effective_to >= effective_from);

-- Optional: ensure only one active version at a time
-- (uncomment later if needed)
-- CREATE UNIQUE INDEX uq_prod_active
--   ON prod_config_plan (prod_code)
--   WHERE effective_to IS NULL;

-- ===============================================
-- 3️⃣ Create new table for benefit configuration
-- ===============================================

-- Create extension for UUIDs (id generation). Use pgcrypto (preferred on Postgres 16).
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Benefits table per product
CREATE TABLE IF NOT EXISTS prod_config_benefits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prod_code VARCHAR(3),
    bene_code VARCHAR(4),
    effective_from DATE,
    effective_to DATE,
    bene_desc VARCHAR(50),
    bene_premium_method VARCHAR(4),
    bene_surrender_method VARCHAR(4),
    bene_lapse_method VARCHAR(4),
    bene_withdrawal_method VARCHAR(4),
    bene_loan_method VARCHAR(4),
    bene_maturity_method VARCHAR(4),
    bene_entry_age_min INT,
    bene_entry_age_max INT,
    bene_term_min INT,
    bene_term_max INT,
    bene_sumassured_min NUMERIC(15,2),
    bene_sumassured_max NUMERIC(15,2),
    bene_premium_min NUMERIC(15,2),
    bene_premium_max NUMERIC(15,2),
    bene_currency VARCHAR(3),
    bene_remarks VARCHAR(100),

    CONSTRAINT uq_prod_benefit UNIQUE (prod_code, bene_code, effective_from),
    CONSTRAINT chk_entry_age CHECK (bene_entry_age_min >= 0 AND bene_entry_age_max >= bene_entry_age_min),
    CONSTRAINT chk_term CHECK (bene_term_min >= 0 AND bene_term_max >= bene_term_min),
    CONSTRAINT chk_effective_dates CHECK (effective_to IS NULL OR effective_to >= effective_from),
    CONSTRAINT chk_sumassured CHECK (bene_sumassured_min >= 0 AND bene_sumassured_max >= bene_sumassured_min)
    
);

-- Trigger to maintain updated_at (N/A for this field so comment below out)
--CREATE OR REPLACE FUNCTION trg_touch_updated_at() RETURNS trigger AS $$
--BEGIN
  --NEW.updated_at := now();
  --RETURN NEW;
--END $$ LANGUAGE plpgsql;

--DROP TRIGGER IF EXISTS tg_touch_updated_at ON prod_config_benefits;
--CREATE TRIGGER tg_touch_updated_at
  --BEFORE UPDATE ON prod_config_benefits
  --FOR EACH ROW EXECUTE FUNCTION trg_touch_updated_at();

COMMENT ON TABLE prod_config_benefits IS 'Benefit-level configuration linked to product master';
COMMENT ON TABLE prod_config_plan IS 'Base product configuration master';

