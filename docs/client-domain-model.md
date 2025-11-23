# Client Domain Model

Purpose: model the client master (AS400 CLNTPF) and KYC checks in a Spring Boot 3 + JPA context.

## Aggregate: Client
- Table: clnt_main
- Identity: clntnum BIGSERIAL (surrogate PK)
- Natural key: unique (clntid_typ, clntid_no)
- Core attributes: surname, givname, cltdob, cltsex, clntid_typ, clntid_no
- Currently mapped fields: see com.policyadmin.client.domain.Client; unmapped legacy columns remain in DB for later enrichment.
- Invariants:
  - clntid_typ and clntid_no required and unique
  - cltsex constrained to legacy single-char values (e.g., M/F/U)
  - datime audit column maintained by DB trigger

## Domain Service: ClientKycValidator
- Package: com.policyadmin.client.kyc
- Input: ClientKycRequest (surname, givname, dateOfBirth, gender, idType, idNumber, ignorePossibleMatch)
- Output: ClientKycValidationResult (status, reasonCode, reasonMessage, matchedClientIds, isOkToCreate())
- Rules:
  1) Duplicate ID → block: DUPLICATE_ID_SAME_PERSON vs DUPLICATE_ID_CONFLICTING_DETAILS
  2) Same person / different ID → flag as POSSIBLE_SAME_PERSON_ID_MISMATCH unless ignorePossibleMatch = true
  3) Otherwise → OK_TO_CREATE_NEW
- Repository queries:
  - indByClntidTypAndClntidNo
  - indByNameDobAndGenderIgnoreCase

## Repository
- ClientRepository extends JpaRepository<Client, Long>
- Purpose: persistence boundary for client aggregate; no direct SQL in services.

## Creation / Update Flow (happy path)
1) Caller runs KYC validation.
2) If esult.isOkToCreate() == true, map request → Client entity and persist via repository.
3) On duplicate/conflict statuses, surface easonCode to caller; do not insert.

## Backlog ideas
- Promote name, gender, ID types to value objects/enums to avoid magic strings.
- Add address/phone/email as embedded value objects when those DB columns are needed.
- Add domain events on creation/update for downstream systems.
