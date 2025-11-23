# Client Database Schema

Source of truth: Flyway migrations under src/main/resources/db/migration.

## Migrations
- V1__init_schema.sql → seed prod_config_t5688 (legacy product config)
- V2__create_second_product_config_table.sql → rename to prod_config_plan, add effective-dating, benefit table prod_config_benefits, constraints, UUID gen
- V3__create_clntpf_table.sql → create clnt_main client master with audit trigger

## clnt_main (client master)
- PK: clntnum BIGSERIAL
- Natural key: unique (clntid_typ, clntid_no)
- Core columns used by app today: surname, givname, cltdob, cltsex, clntid_typ, clntid_no
- Additional legacy columns retained (addresses, phones, nationality, etc.) for future mapping
- Audit: datime TIMESTAMP auto-set by trigger 	rg_clnt_main_datime on insert/update
- JPA entity: com.policyadmin.client.domain.Client maps subset of columns

## prod_config_plan (product config, effective-dated)
- PK: (prod_code, effective_from)
- Columns: prod_desc, prod_category, effective period, premium mode flags (is_single_prem, is_annual, is_half_yearly, is_quarterly, is_monthly)
- Constraint: ck_effective_dates ensures effective_to >= effective_from

## prod_config_benefits (benefit-level config)
- PK: id UUID DEFAULT gen_random_uuid()
- Natural key: (prod_code, bene_code, effective_from)
- Attributes: benefit desc, pricing/surrender/lapse/maturity method codes, age/term bounds, sum assured/premium ranges, currency, remarks
- Constraints: checks on age, term, sum assured ranges; effective date validity

## Conventions & Notes
- Schema assumes PostgreSQL with pgcrypto for UUIDs.
- Flyway is baseline-on-migrate to coexist with pre-existing objects.
- JPA ddl-auto is 
one; schema managed exclusively via Flyway.
- Future: add indexes for common lookups (e.g., lower(surname), lower(givname), cltdob, cltsex), map remaining clnt_main columns as value objects.
