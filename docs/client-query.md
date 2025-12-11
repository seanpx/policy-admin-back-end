````markdown
# Client Enquiry API – Design Summary (v1)

## 1. Scope and Objectives

This document describes the initial design of the **Client Enquiry** function for the Policy Admin Portal POC.

**Current scope (v1):**

- Data source: `clnt_main` (only; `clnt_alt` is out of scope for now).
- Search criteria:
  - `clntIdNo` – full or partial, case-insensitive.
  - `surname` – full or partial, case-insensitive.
  - `givname` – full or partial, case-insensitive.
- All criteria are **optional**:
  - user can search by any combination of these fields;
  - null/blank inputs are ignored.
- Results:
  - Paginated list of lightweight DTOs (no heavy entity graph).
- Persistence approach:
  - `JpaSpecificationExecutor` + `Specification` + Criteria DTO.

This is the Java/Spring equivalent of a CLNTPF enquiry subfile with flexible selection options.

---

## 2. High-Level Design

### 2.1 Endpoint

- **HTTP**: `POST /api/clients/enquiry`
- **Request body** (all optional, partial match):
  - `clntIdNo`, `surname`, `givname`
- **Pagination** via query params: `page`, `size`, `sort`.

**Example:**

```http
POST /api/clients/enquiry?page=0&size=20
Content-Type: application/json

{
  "clntIdNo": "123",
  "surname": "tan",
  "givname": "ah"
}
````

### 2.2 Request/Response Contract

* Input is via JSON body → mapped into a **Criteria DTO** in the controller layer.
* Output is a paginated list of **ClientSummaryDto**:

```json
{
  "content": [
    {
      "clntnum": 42,
      "clntIdNo": "S1234567A",
      "surname": "TAN",
      "givname": "AH KOW"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 123,
  "totalPages": 7
}
```

---

## 3. Package and File Structure

Suggested structure under the `com.policyadmin.client` bounded context:

* `/src/main/java/com/policyadmin/client/api/`

  * `ClientEnquiryController.java`
* `/src/main/java/com/policyadmin/client/api/dto/`

  * `ClientEnquiryCriteria.java`
  * `ClientSummaryDto.java`
* `/src/main/java/com/policyadmin/client/domain/`

  * `ClientMainEntity.java`
* `/src/main/java/com/policyadmin/client/persistence/`

  * `ClientRepository.java`
  * `ClientSpecifications.java`
* `/src/main/java/com/policyadmin/client/service/`

  * `ClientEnquiryService.java`

This keeps API, domain, persistence, and service concerns separate and future-proof for extra enquiry complexity.

---

## 4. Domain Model (clnt_main)

Entity representing `clnt_main` (simplified):

```java
// /src/main/java/com/policyadmin/client/domain/ClientMainEntity.java
@Entity
@Table(name = "clnt_main")
public class ClientMainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;          // or UUID

    @Column(name = "clntid_no")
    private String clntIdNo;

    @Column(name = "surname")
    private String surname;

    @Column(name = "givname")
    private String givname;

    @Column(name = "status")
    private String status;    // ACTIVE, CLOSED, etc.

    // getters and setters
}
```

---

## 5. Criteria DTO

Encapsulates all possible enquiry inputs (current and future) instead of passing raw query params around.

```java
// /src/main/java/com/policyadmin/client/api/dto/ClientEnquiryCriteria.java
public class ClientEnquiryCriteria {

    private String clntIdNo;
    private String surname;
    private String givname;

    // Future: gender, dob, address, etc.

    // getters and setters
}
```

Responsibilities:

* Collected/constructed in `ClientEnquiryController`.
* Passed down to `ClientEnquiryService` and then `ClientSpecifications`.

---

## 6. Result DTO

DTO to return lightweight results to the frontend.

```java
// /src/main/java/com/policyadmin/client/api/dto/ClientSummaryDto.java
public class ClientSummaryDto {

    private Long clientId;     // or UUID
    private String clntIdNo;
    private String surname;
    private String givname;
    private String status;

    // constructors, getters, setters
}
```

No nested relationships or heavy fields (e.g., addresses, audit, etc.) in this first version.

---

## 7. Repository and Specification

### 7.1 Repository

```java
// /src/main/java/com/policyadmin/client/persistence/ClientRepository.java
public interface ClientRepository extends
        JpaRepository<ClientMainEntity, Long>,
        JpaSpecificationExecutor<ClientMainEntity> {
}
```

Extending `JpaSpecificationExecutor` allows us to pass a `Specification<ClientMainEntity>` into `findAll(spec, pageable)`.

### 7.2 Specification Builder

Central place to convert `ClientEnquiryCriteria` into a `Specification<ClientMainEntity>`.

Key design rules:

* Ignore null/blank params.
* Trim whitespace.
* Case-insensitive search:

  * `LOWER(column) LIKE LOWER('%value%')`.
* Wildcard both sides for partial match.
* Optionally enforce minimum length (e.g. ≥ 2 or 3 chars) at service/controller level before calling this spec.

```java
// /src/main/java/com/policyadmin/client/persistence/ClientSpecifications.java
public final class ClientSpecifications {

    private ClientSpecifications() {}

    public static Specification<ClientMainEntity> byCriteria(ClientEnquiryCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            addClntIdNoPredicate(c, root, cb, predicates);
            addSurnamePredicate(c, root, cb, predicates);
            addGivnamePredicate(c, root, cb, predicates);

            // If no criteria provided, behaviour is defined at service/controller level
            // (e.g., reject or allow broad search with a capped page size).

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addClntIdNoPredicate(
            ClientEnquiryCriteria c,
            Root<ClientMainEntity> root,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        String value = normalize(c.getClntIdNo());
        if (value != null) {
            predicates.add(
                cb.like(cb.lower(root.get("clntIdNo")), "%" + value + "%")
            );
        }
    }

    private static void addSurnamePredicate(
            ClientEnquiryCriteria c,
            Root<ClientMainEntity> root,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        String value = normalize(c.getSurname());
        if (value != null) {
            predicates.add(
                cb.like(cb.lower(root.get("surname")), "%" + value + "%")
            );
        }
    }

    private static void addGivnamePredicate(
            ClientEnquiryCriteria c,
            Root<ClientMainEntity> root,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        String value = normalize(c.getGivname());
        if (value != null) {
            predicates.add(
                cb.like(cb.lower(root.get("givname")), "%" + value + "%")
            );
        }
    }

    private static String normalize(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }
}
```

---

## 8. Service Layer

Service coordinates validation, specification building, repository call, and mapping to DTO.

```java
// /src/main/java/com/policyadmin/client/service/ClientEnquiryService.java
@Service
public class ClientEnquiryService {

    private final ClientRepository clientRepository;

    public ClientEnquiryService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Page<ClientSummaryDto> enquiry(ClientEnquiryCriteria criteria, Pageable pageable) {
        // Basic safety: avoid totally empty search unless explicitly allowed
        if (isAllEmpty(criteria)) {
            // Option 1: throw validation exception
            // Option 2: allow, but enforce small page size at controller
        }

        Specification<ClientMainEntity> spec = ClientSpecifications.byCriteria(criteria);

        Page<ClientMainEntity> page = clientRepository.findAll(spec, pageable);

        return page.map(this::toSummaryDto);
    }

    private boolean isAllEmpty(ClientEnquiryCriteria criteria) {
        return isBlank(criteria.getClntIdNo())
                && isBlank(criteria.getSurname())
                && isBlank(criteria.getGivname());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private ClientSummaryDto toSummaryDto(ClientMainEntity entity) {
        ClientSummaryDto dto = new ClientSummaryDto();
        dto.setClientId(entity.getId());
        dto.setClntIdNo(entity.getClntIdNo());
        dto.setSurname(entity.getSurname());
        dto.setGivname(entity.getGivname());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
```

Responsibilities:

* Optional enforcement of minimum length / non-empty criteria.
* Translating `ClientMainEntity` → `ClientSummaryDto`.
* Delegating query construction to `ClientSpecifications`.

---

## 9. Controller Layer

Controller accepts JSON body, maps it into `ClientEnquiryCriteria`, and delegates to the service.

```java
// /src/main/java/com/policyadmin/client/api/ClientEnquiryController.java
@RestController
@RequestMapping("/api/clients")
public class ClientEnquiryController {

    private final ClientEnquiryService clientEnquiryService;

    public ClientEnquiryController(ClientEnquiryService clientEnquiryService) {
        this.clientEnquiryService = clientEnquiryService;
    }

    @PostMapping("/enquiry")
    public Page<ClientSummaryDto> enquiry(
            @RequestBody(required = false) ClientEnquiryCriteria criteria,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ClientEnquiryCriteria c = criteria == null ? new ClientEnquiryCriteria(null, null, null) : criteria;
        return clientEnquiryService.enquiry(c, pageable);
    }
}
```

---

## 10. Behaviour and Matching Rules

1. **Case-insensitive:**

   * All comparisons use `LOWER(column) LIKE LOWER('%value%')`.

2. **Partial match:**

   * Wildcard both sides: `%value%`.
   * Supports “contains” semantics (not just prefix).

3. **Optional parameters:**

   * Null/blank values are ignored; they do not add any predicate.
   * Users can search with only one or any combination of fields.

4. **Validation / safety:**

   * Enforce minimum search string length (e.g. ≥ 2 chars) to prevent full-table scans due to `LIKE '%%'`.
   * Consider rejecting completely empty criteria with a 400 error or limiting page size.

5. **Pagination:**

   * Use Spring Data `Pageable`.
   * Page size capped to a reasonable maximum (e.g. 50 or 100) at controller / config level.

---

## 11. Testing Strategy

### 11.1 Unit Tests (Specification)

* Target: `ClientSpecifications.byCriteria(...)`.
* Scenarios:

  * Only `clntIdNo` populated.
  * Only `surname` populated.
  * Only `givname` populated.
  * Combination of all three.
  * Null/blank values.
* Validate:

  * Generated predicates count / behaviour (e.g. via integration tests with Testcontainers Postgres).
  * Matching is case-insensitive.

### 11.2 Service Tests

* Target: `ClientEnquiryService`.
* Use Testcontainers + Postgres for integration tests (preferred), or H2 if simpler initially.
* Seed sample `clnt_main` data and test:

  * Partial string matches.
  * Pagination behaviour.
  * Empty criteria handling.

### 11.3 Controller Tests

* MockMvc or WebTestClient tests for:

  * Correct mapping from query params → `ClientEnquiryCriteria`.
  * Proper HTTP status codes and JSON structure.
  * Pagination query params (`page`, `size`) handled correctly.

---

## 12. Future Extensions (Beyond v1)

The design intentionally leaves space for:

* Additional criteria in `ClientEnquiryCriteria`:

  * `gender`, `dob`, `status`, `address`, etc.
* `ClientSpecifications` can be extended with new helper methods per field.
* Adding `ClientAltAddressEntity` and extending `addAddressPredicate(...)` to join `clnt_alt` for address/contacts search.
* Migrating to QueryDSL later if search logic becomes significantly more complex (multi-join, cross-domain queries).

For now, `JpaSpecificationExecutor + Specification + Criteria DTO` provides a clean, incremental path to build up the enquiry capability while keeping the codebase understandable and testable.

```
```
