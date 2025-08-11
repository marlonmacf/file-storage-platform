# File Storage Platform

A Spring Boot + Angular application to upload and store arbitrary files (any type) in an H2 in-memory database as BLOBs. Goal is to experiment and include full testing coverage: architecture rules, unit tests, integration tests, and end-to-end tests with Cypress.

---

## Technologies & Patterns Used (What I Learned)

TODO: ...

---


## Architecture Overview

**Purpose:**
- TODO: ...

**Key Responsibilities:**
- **Any File Upload** via REST (`POST /files/upload`)
- **List Files** with metadata (`GET /files`)
- **Download File** by ID (`GET /files/{id}`)
- Store files in H2 using `@Lob` BLOB storage
- Layered architecture with best practices

```
backend/
├── src/main/java/...
│   ├── controller/...    ← (handles upload, list, download endpoints)
│   ├── service/...       ← (validates, transforms metadata, and saves files)
│   ├── repository/...    ← (JPA repository for file persistence)
│   ├── model/...         ← (holds metadata + binary content (byte[]))
│   └── util/...          ← (optional validators)
└── src/test/java/...
    ├── architecture/...  ← (ensures layered independence using ArchUnit)
    ├── integration/...   ← (Spring Boot + H2 using @SpringBootTest and @DataJpaTest)
    └── unit/...          ← (isolated component behavior via JUnit + Mockito)

frontend/
└── src/
    ├── app/...
    └── cypress/...       ← (E2E: Cypress tests HTTP endpoints + UI end-to-end flow)

files-to-test/...         ← (sample csv, json, png, and txt for testing)
```

---

## Testing Strategy

### 1. Architecture Rules

Using ArchUnit to enforce:

- Naming/package conventions are enforced (e.g., classes ending with `Controller`, residing in proper packages)
- Services & Repositories **do not** depend on Controllers
- Controllers **do not** directly access Repositories

### 2. Unit Tests

Isolated tests per component:

- **Controller** (`@WebMvcTest`): HTTP mappings, validation, error handling
- **Service** (Mockito + JUnit): metadata logic, exception cases
- **Repository** (`@DataJpaTest`): custom queries (if any)
- **Utility Classes** (JUnit): pure logic

### 3. Integration Tests

Real application context with H2 DB:

- **`@SpringBootTest`** – end-to-end file upload + download flow
- **`@DataJpaTest`** – verify BLOB storage and retrieval
- **MockMvc** – full HTTP route integration without UI

### 4. End-to-End (E2E)

Using Cypress to simulate:

- File upload via UI
- Files listing and download
- Validation of HTTP headers and file integrity

---

## Getting Started

### Prerequisites

- Java 17+ or your preferred Java version
- Maven 3.8+
- Node.js + npm (for Angular)
- Cypress (installed via npm)

### Backend Setup
   ```
   mvn clean install
   mvn spring-boot:run
   ```

App will start on `http://localhost:8080`.

### Frontend Setup

```
cd frontend
npm install
npm run start
```

Angular app runs on `http://localhost:4200`.

### Cypress (E2E)

```
cd frontend
npm run cypress:open
```

Run Cypress tests against `http://localhost:4200`.

---

### Cursor + GPT 5

## #1 Task

I’m going to add a JaCoCo coverage plugin to file-storage-service/pom.xml, to be able to run the test suite with coverage enabled, generate the report, and extract a concise per-class coverage summary.

Running Maven verify to execute tests and generate JaCoCo coverage, then I’ll read the generated JaCoCo CSV/XML and summarize per-class coverage.

mvn -f file-storage-service/pom.xml -q -DskipTests=false test
mvn -f file-storage-service/pom.xml -q verify

I’m going to locate the generated JaCoCo report files and read the CSV/XML so I can summarize per-class coverage.

file:///home/mandrel/Documents/file-storage-platform/file-storage-service/target/site/jacoco/index.html

I added JaCoCo, ran the tests with coverage, and parsed the generated report to summarize per-class coverage.

## #2 Task

I’m about to add comprehensive tests: controller tests with WebMvcTest, service tests with Mockito/JUnit, repository tests with DataJpaTest, and DTO utility tests with JUnit. Then I’ll run the test suite and report coverage.

I’m going to create the necessary test directories, then add controller, service, repository, and DTO tests. After that, I’ll run the tests and report coverage.

## 3# Task

Creating integration tests: one for end-to-end memory storage flow with @SpringBootTest + @AutoConfigureMockMvc, one for local filesystem flow using MockMvc and cleaning up files, and a repository test to verify BLOB save/retrieval with @DataJpaTest. Then I’ll run the full test suite.

## #4 Task

Negative/edge-case HTTP tests: empty file, missing file part, oversized file (>10MB), unknown content types, filename with path traversal. Assert 400/413 and error bodies.

## #5 Task

Added concurrency tests in ConcurrencyIntegrationTest:
    • Parallel uploads to memory (/files/memory/save) using a thread pool; asserts all IDs returned and each file downloads successfully.
    • Parallel downloads from memory; ensures each downloaded payload matches what was uploaded.
    • Parallel uploads to local filesystem; validates content on download. Added cleanup of uploaded-files/ after each run for isolation.
  • Implemented negative/edge-case HTTP tests in NegativeEdgeCasesControllerTest:
    • Empty file, missing part, unknown content type, path traversal, and oversized payload. Configured a lower multipart limit for this
      test class via @TestPropertySource to reliably assert 413.
  • Fixed controller constructors so tests don’t require MultipartProperties beans; they now read spring.servlet.multipart.max-file-size
    via @Value and do size checks manually.

## #6 Task

Large file/streaming tests: verify memory usage and headers on multi‑MB files; ensure download streams correctly.

## #7 Task

JSON serialization tests (`@JsonTest`): assert StoredFileDto JSON shape, null handling, and date format.

## #8 Task
REST API contract tests: Rest Assured with OpenAPI schema validation (springdoc + swagger-request-validator) to prevent breaking changes.

## #9 Task
DB-specific integration with Testcontainers: run against PostgreSQL (or your target DB) to verify BLOB behavior beyond H2.

## #10 Task
Migration tests: if you add Flyway/Liquibase, validate migrations up/down and data preservation.

## #11 Task
Security tests: with Spring Security Test (roles, unauthorized/forbidden, CSRF) if/when security is added.

## #12 Task
Mutation testing: PIT (pitest-maven) to ensure tests catch real code changes.

## #13 Task
Property-based tests: jqwik for filenames, binary sizes, and weird content inputs.

## #14 Task
Performance/load: Gatling or k6 scenarios for upload/download throughput and tail latency.

## #15 Task
Filesystem failure/permissions: simulate IO failures (e.g., with jimfs or by mocking Files.* to throw IOException) and assert 5xx handling.

## #16 Task
Contract tests (Pact): if a separate UI/service consumes this API, add CDC tests to lock the interface.

## #17 Task
ArchUnit extensions: add rules for package cycles, annotations usage, and public API exposure.

## #18 Task
Build/quality gates: JaCoCo “check” rule with thresholds; SpotBugs/Checkstyle for static analysis.

---

I reviewed the working tree and recent history, then split the changes into granular, chronological commits aligned to your tasks.