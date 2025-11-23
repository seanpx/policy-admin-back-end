# Client API Design

Goal: expose KYC validation and client onboarding while keeping the web layer thin and delegating rules to domain services.

## Resources & Endpoints

### Health
- GET /actuator/health → Spring Boot actuator

### Greeting (placeholder)
- GET /api/hello → existing sample controller

### Client KYC
- POST /api/clients/kyc/validate
  - Request (JSON):
    `json
    {
      "surname": "Doe",
      "givname": "John",
      "dateOfBirth": "1990-01-01",
      "gender": "M",
      "idType": "PAS",
      "idNumber": "A1234567",
      "ignorePossibleMatch": false
    }
    `
  - Response 200 OK:
    `json
    {
      "status": "POSSIBLE_SAME_PERSON_ID_MISMATCH",
      "reasonCode": "POSSIBLE_MATCH_ID_MISMATCH",
      "reasonMessage": "Client with matching personal details but different identification found.",
      "matchedClientIds": [42]
    }
    `
  - Error cases: 400 for validation errors, 500 for unexpected failures.

### Client Creation (planned)
- POST /api/clients
  - Precondition: caller must have run KYC and received OK_TO_CREATE_NEW (or override reason).
  - Request body: client demographics + ID; server re-runs KYC defensively before insert.
  - Response: 201 Created with { "clientId": <clntnum> } on success.
  - Failure: 409 Conflict when KYC fails (duplicate/conflict), payload echoes easonCode/matchedClientIds.

### Client Retrieval (planned)
- GET /api/clients/{clntnum} → fetch client profile (limited fields initially: name, dob, gender, idType, idNumber).
- GET /api/clients → paged search by name/id (later).

## Contracts & Validation
- Names case-insensitive for matching; stored as provided.
- idType: 3-char legacy code (e.g., PAS, NRIC, DL).
- gender: single-char legacy code (M/F/U).
- dateOfBirth: ISO-8601 YYYY-MM-DD.

## Security
- Currently basic auth (Spring default creds in pplication.yml); later JWT/Keycloak.
- Prefer stateless APIs; no session state.

## Error Model
- Use RFC 7807 style payload:
  `json
  { "type": "https://policyadmin/errors/kyc-conflict", "title": "Duplicate ID", "detail": "...", "reasonCode": "DUPLICATE_ID", "matchedClientIds": [1,2] }
  `
- Map to HTTP status codes: 409 for conflicts, 400 for bad input, 500 for server errors.

## Open Tasks
- Implement controllers for the KYC validate + create endpoints.
- Add request DTOs with bean validation annotations.
- Add API tests (MockMvc) for the KYC paths.
