# 🏛️ Policy Administration Portal (Backend Service)

Spring Boot (Maven) service.

## 📖 Overview
This backend service is part of the **Java Policy Administration Portal (POC)** — a personal proof of concept project by **Sean Pan (Pan Xiang)** to reimagine a traditional AS400/Life400 insurance policy admin system in a **modern Java + Spring Boot architecture**.

The goal is to simulate one complete **life insurance policy lifecycle**:
> Quotation → Proposal → Underwriting → Issuance → Servicing

while progressively learning **domain-driven design (DDD)**, **clean architecture**, and **cloud-native development** practices.

---

## 🧩 Architecture

| Layer | Technology | Description |
| :---- | :---------- | :----------- |
| **Language** | Java 21 (Temurin) | Modern Java features (Records, Pattern Matching, Streams) |
| **Framework** | Spring Boot 3.x | REST APIs, Dependency Injection, Actuator |
| **Persistence** | JPA (Hibernate) | Maps domain entities to PostgreSQL |
| **Migration** | Flyway | Database versioning and schema evolution |
| **Database** | PostgreSQL (via Docker) | Persistent backend data store |
| **Testing** | JUnit 5 + Testcontainers | Integration testing with real DB containers |
| **Security (later)** | JWT / Keycloak | Token-based authentication (dev mode first) |
| **Build Tool** | Maven | Dependency and lifecycle management |

---

## 🧠 Domain Focus

This project bridges **AS400 insurance logic** to modern Java concepts.

| AS400 Concept | Modern Equivalent |
| :------------- | :---------------- |
| Record structures | Domain entities / aggregates |
| Status tables | Enum-based state machines |
| Batch jobs | Scheduled tasks / Async jobs |
| Message queues | Spring events / Kafka (future) |
| Policy ledger | JPA repositories + transactional integrity |

---

## 📦 Folder Structure

/src
 ├─ main/java/com/pas/policy_admin_back_end/
 │   ├─ controller/     → REST endpoints (HTTP adapters kept thin)
 │   ├─ dto/            → Request/response contracts
 │   ├─ service/        → Business logic orchestrators
 │   ├─ domain/         → Entities, value objects
 │   └─ config/         → App and DB configs
 └─ main/resources/
     ├─ application.yml
     └─ db/migration/

Controllers validate/map HTTP requests and then hand work to services so that policy logic stays outside the web layer.

---

## Build

```bash
./mvnw clean package
```

On Windows:

```bat
mvnw.cmd clean package
```

## Run

```bash
./mvnw spring-boot:run
```

The app reads configuration from `src/main/resources/application.yml`.

---

## 🧪 Testing Strategy

Unit Tests: Focus on service & domain logic

Integration Tests: Using Testcontainers to run PostgreSQL inside Docker

API Tests: Spring Boot test slices with MockMvc

---

## 🚀 Future Enhancements

Implement domain events for lifecycle transitions

Introduce Keycloak authentication

Deploy on AWS (ECS / RDS)

Add monitoring with Spring Actuator + Prometheus

Migrate to microservices per domain (NB, UW, Servicing)

## 🧭 Author & Intent

Author: Sean Pan (Pan Xiang)
Role: Insurance domain expert (AS400/Life400) learning modern full-stack architecture
Purpose: Build practical hands-on mastery in Spring Boot + React + Cloud development while mapping legacy insurance patterns to modern systems.


---

### 💬 Notes for You

This version is **Codex-friendly** — meaning:
- GPT-Codex will read the “Current Sprint Tasks” and “Modules” sections to infer what code or tests to generate next.
- It’s ready to serve as the default context file when you open your backend repo in VS Code (Codex treats `README.md` as high-priority input).

---

Would you like me to now generate a **matching `README.md`** for your **frontend (React) service** — following the same pattern but focusing on Vite, shadcn/ui, and React Query setup?
