You are my technical co-architect for a Spring Boot 3 + Java 21 policy admin POC.

Context:
- I have a Postgres table clnt_main (like CLNTPF in AS400) with these key columns:
  - clntnum BIGSERIAL PRIMARY KEY
  - surname VARCHAR(100)
  - givname VARCHAR(100)
  - cltdob DATE
  - cltsex VARCHAR(1)
  - clntid_typ VARCHAR(3) NOT NULL
  - clntid_no  VARCHAR(50) NOT NULL
- There is a unique constraint on (clntid_typ, clntid_no) so duplicate IDs are blocked at DB level.
- I want a reusable “client KYC validation” function that checks incoming client data before creating a new record.

Functional rules:
1) First, check if any existing client has the same ID (clntid_typ, clntid_no).
   - If found AND surname + givname + cltdob + cltsex match (case-insensitive on names), treat as “DUPLICATE_ID_SAME_PERSON”.
   - If found but the personal details do NOT match, treat as “DUPLICATE_ID_CONFLICTING_DETAILS”.
   - In both cases, we must NOT create a new client. We need to return a structured reason code.
   - This rule (ID-level uniqueness) must ALWAYS apply and must NOT be bypassable.

2) If no matching ID is found, check for same surname + givname + cltdob + cltsex (potential same person with different ID).
   - If any such client exists but with a different ID (or ID missing), treat as “POSSIBLE_SAME_PERSON_ID_MISMATCH”.
   - By default, we must NOT create a new client in this case. Caller should investigate or update the existing client’s ID through a separate flow.

3) If neither rule finds a match, treat as “OK_TO_CREATE_NEW”.

New requirement – override toggle:
- The validator must support a boolean control toggle to allow bypassing rule (2) for POSSIBLE matches, AFTER manual verification by the user.
- Add a flag `ignorePossibleMatch` in the input DTO (`ClientKycRequest`).
- Behavior:
  - If `ignorePossibleMatch = false` (default):
    - Apply all rules as described above (including rule 2).
  - If `ignorePossibleMatch = true`:
    - Still apply rule (1) for exact ID duplicates (this must NEVER be ignored).
    - SKIP rule (2) for “same name + DOB + gender but different ID”.
    - In this case, even if a possible match is found under rule (2), the validator should treat the request as “OK_TO_CREATE_NEW” and return a suitable reason code (for example “OK_OVERRIDE_POSSIBLE_MATCH”) to indicate that an override was used.

Design requirements:
- Implement a domain service class `ClientKycValidator` under package:
  `com.policyadmin.client.kyc`.
- Create:
  - a simple input DTO `ClientKycRequest` with fields:
    - surname (String)
    - givname (String)
    - dateOfBirth (LocalDate)
    - gender (String)
    - idType (String)
    - idNumber (String)
    - ignorePossibleMatch (boolean)  // if true, skip rule (2) and allow creation despite possible match
  - an enum `ClientKycStatus` with values:
    - OK_TO_CREATE_NEW,
    - DUPLICATE_ID_SAME_PERSON,
    - DUPLICATE_ID_CONFLICTING_DETAILS,
    - POSSIBLE_SAME_PERSON_ID_MISMATCH.
  - a result record `ClientKycValidationResult` containing:
    - status (ClientKycStatus)
    - reasonCode (String)
    - reasonMessage (String)
    - matchedClientIds (List<Long>)
    - plus a helper method `isOkToCreate()`.

- Implement the service method:
  `public ClientKycValidationResult validate(ClientKycRequest request)`.

- Status / reasonCode guidance:
  - If rule (1) finds a match and personal details match:
    - status = DUPLICATE_ID_SAME_PERSON
    - reasonCode = "DUPLICATE_ID"
  - If rule (1) finds a match and personal details do NOT match:
    - status = DUPLICATE_ID_CONFLICTING_DETAILS
    - reasonCode = "ID_CONFLICT"
  - If rule (2) finds a match and `ignorePossibleMatch = false`:
    - status = POSSIBLE_SAME_PERSON_ID_MISMATCH
    - reasonCode = "POSSIBLE_MATCH_ID_MISMATCH"
  - If rule (2) finds a match but `ignorePossibleMatch = true`:
    - status = OK_TO_CREATE_NEW
    - reasonCode = "OK_OVERRIDE_POSSIBLE_MATCH"
  - If no match is found:
    - status = OK_TO_CREATE_NEW
    - reasonCode = "OK"

- The service must be `@Service`, use constructor injection, and depend on an existing JPA repository `ClientRepository` which manages the `Client` entity mapped to `clnt_main`.
- The service method must be `@Transactional(readOnly = true)` and must NOT write to the database.
- Implement the necessary Spring Data repository methods in `ClientRepository`:
  - `List<Client> findByClntidTypAndClntidNo(String idType, String idNumber);`
  - and a custom `@Query` method to find by surname + givname + cltdob + cltsex with case-insensitive comparison on the names.

- Use Java 21 record types where appropriate (for DTOs and results).
- Put the entity in `com.policyadmin.client.domain.Client` and the repository in `com.policyadmin.client.repository.ClientRepository`.
- Make the code clean, idiomatic, and ready to compile in a Spring Boot 3 project.

- Also generate a basic JUnit 5 test class `ClientKycValidatorTest` using Mockito to cover at least:
  - OK_TO_CREATE_NEW (no matches),
  - DUPLICATE_ID_SAME_PERSON,
  - POSSIBLE_SAME_PERSON_ID_MISMATCH with `ignorePossibleMatch = false`,
  - OK_TO_CREATE_NEW with `ignorePossibleMatch = true` while a possible match exists.

Please generate all necessary Java files with proper package declarations and imports.
